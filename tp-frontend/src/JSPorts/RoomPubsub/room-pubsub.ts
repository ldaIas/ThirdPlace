import { createLibp2p, Libp2p } from 'libp2p';
import { webRTC } from '@libp2p/webrtc';
import { webSockets } from '@libp2p/websockets';
import { noise } from '@chainsafe/libp2p-noise';
import { yamux } from '@chainsafe/libp2p-yamux';
import { gossipsub } from '@chainsafe/libp2p-gossipsub';
import { identify, identifyPush, Identify, IdentifyPush } from '@libp2p/identify';
import { circuitRelayTransport } from '@libp2p/circuit-relay-v2';
import * as filters from '@libp2p/websockets/filters';
import { ping, Ping } from '@libp2p/ping';
import { multiaddr } from '@multiformats/multiaddr';

import type { App } from '../../types/apptypes'

// This is all because libp2p-gossipsub doesn't expose PubSub
//    First ReturnType gives you the factory function type: (components: GossipSubComponents) => PubSub<GossipSubEvents>
//    Second ReturnType on that factory gives the actual instance: PubSub<GossipSubEvents>
type GossipSubService = ReturnType<ReturnType<typeof gossipsub>>

/**
 * Services used by the libp2p node for the room communication
 */
export type RoomServices = {
    pubsub: GossipSubService // Using this since libp2p-gossipsub doesn't export PubSub
    identify: Identify
    identifyPush: IdentifyPush
    ping: Ping
}

interface MessageEventDetail {
    data: Uint8Array; // Assuming the data is of type Uint8Array
}

let node: Libp2p<RoomServices>;
let topic: string;

// Currently retrieved manually when starting the relay server
const RELAY_MULTIADDR = multiaddr("/ip4/127.0.0.1/tcp/9090/ws/p2p/12D3KooWKnTok2TigFutGyYTF6E9utiTkzv1Dx8Vjx3tYsQwm84z");

export async function setupRoomPubSubPorts(app: App): Promise<void> {

    // When joining a room, create a libp2p node to begin connecting to the relay and peers
    app.ports.joinRoom.subscribe(async (roomId) => {
        console.log("Joining room", roomId);
        topic = `tp-${roomId}`;

        try {
            node = await createLibp2p<RoomServices>({
                transports: [
                    webSockets({
                        filter: filters.all
                    }),
                    webRTC(),
                    circuitRelayTransport()
                ],
                connectionEncrypters: [noise()],
                streamMuxers: [yamux()],
                connectionGater: {
                    // Allow private addresses for local testing
                    denyDialMultiaddr: () => false
                },
                services: {
                    pubsub: gossipsub(),
                    identify: identify(),
                    identifyPush: identifyPush(),
                    ping: ping()
                },
                addresses: {
                    listen: [
                        '/p2p-circuit',
                        '/webrtc'
                    ]
                }
            });

            console.log("node peerId: ", node.peerId);

            node.addEventListener('peer:connect', (event: any) => {
                console.log("connection established to ", event.detail.remotePeer.toString());
            });

            node.addEventListener('peer:discovery', async (event: any) => {
                console.log('peer:discovery event:', JSON.stringify(event.detail))
                const peer = event.detail.id;
                console.log("Discovered peer:", peer.toString());

                try {
                    await node.dial(multiaddr(peer));
                    console.log("✅ Connected to discovered peer:", peer.toString());
                } catch (err) {
                    console.warn("❗Failed to connect to peer:", err);
                }
            });

            console.log("Services available:", Object.keys(node.services));

            node.services.pubsub.addEventListener('message', (msg: CustomEvent<MessageEventDetail>) => {
                const messageJson = new TextDecoder().decode(msg.detail.data);
                console.log("Received message:", messageJson);
                try {
                    const message = JSON.parse(messageJson);
                    // If the json has "intro" then it is a new conversation. Otherwise it is a chat message
                    if (message['intro']) {
                        app.ports.receiveConversation.send(message);
                    } else {
                        app.ports.receiveMessage.send(message);
                    }
                } catch (err) {
                    console.error("Invalid message received:", messageJson, err);
                }
            })
            await node.services.pubsub.subscribe(topic);

            // Try to connect to the relay
            try {
                await node.dial(RELAY_MULTIADDR);
                console.log("Connected to relay:", RELAY_MULTIADDR);
            } catch (err) {
                console.error("Failed to connect to relay:", err);
            }

            console.log(`Node started. Subscribing to ${topic}`);
            console.log(`node multiaddr`, node.getMultiaddrs());
            console.log('node', node);

        } catch (err) {
            console.error("Failed to create node:", err);
        }


    });

    app.ports.sendMessage.subscribe(async (message) => {
        if (node && topic) {
            const messageWithTimestamp = {
                ...message,
                timestamp: Date.now()
            };
            const jsonMsg = JSON.stringify(messageWithTimestamp);
            console.log('attempting to send message to topic', topic);
            await node.services.pubsub.publish(topic, new TextEncoder().encode(jsonMsg));
        } else {
            console.warn("PubSub not initialized yet");
        }
    });

}
