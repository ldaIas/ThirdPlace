import { createLibp2p } from 'libp2p';
import { webSockets } from '@libp2p/websockets';
import { webRTCStar } from '@libp2p/webrtc-star';
import { pubsubPeerDiscovery } from '@libp2p/pubsub-peer-discovery'
import { noise } from '@chainsafe/libp2p-noise';
import { yamux } from '@chainsafe/libp2p-yamux';
import { gossipsub } from '@chainsafe/libp2p-gossipsub';
import { identify } from '@libp2p/identify';
import { circuitRelayTransport } from '@libp2p/circuit-relay-v2';
import { peerIdFromString } from '@libp2p/peer-id';

let node;
let topic;
const webrtcStar = webRTCStar()

// Currently retrieved manually when starting the relay server
const RELAY_MULTIADDR = "/ip4/127.0.0.1/tcp/9090/ws/p2p/12D3KooWCEKn8AnuMfWADH17zsAj2PnuWAaFHDwgY4Z9i3MXjUaX";

export async function setupRoomPubSubPorts(app) {

    app.ports.joinRoom.subscribe(async (roomId) => {
        console.log("Joining room", roomId);
        topic = `tp-${roomId}`;

        try {
            node = await createLibp2p({
                transports: [webSockets(), webrtcStar.transport, circuitRelayTransport()],
                connectionEncrypters: [noise()],
                streamMuxers: [yamux()],
                peerDiscovery: [webrtcStar.discovery, pubsubPeerDiscovery({interval: 100, topics: [topic]})],
                connectionGater: {
                    // Allow private addresses for local testing
                    denyDialMultiaddr: () => false
                },
                services: {
                    pubsub: gossipsub(),
                    identify: identify(),
                },
                addresses: {
                    listen: ["/webrtc"]
                }
            });

            node.addEventListener('peer:connect', (event) => {
                console.log("connection established to ", event.detail.remotePeer.toString());
            });

            node.addEventListener('peer:discovery', async (event) => {
                const peer = event.detail;
                console.log("Discovered peer:", peer.toString());

                try {
                    await node.dial(peer);
                    console.log("✅ Connected to discovered peer:", peer.toString());
                } catch (err) {
                    console.warn("❗Failed to connect to peer:", err);
                }
            });

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

        console.log("Services available:", Object.keys(node.services));
        await node.services.pubsub.subscribe(topic, (msg) => {
            const messageJson = new TextDecoder().decode(msg.data);
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
                console.error("Invalid message received:", messageJson, err)
            }
        })
    })

    app.ports.sendMessage.subscribe(async (message) => {
        if (node && topic) {
            const messageWithTimestamp = {
                ...message,
                timestamp: Date.now()
            }
            const jsonMsg = JSON.stringify(messageWithTimestamp)
            console.log('attempting to send message to topic', topic)
            await node.services.pubsub.publish(topic, new TextEncoder().encode(jsonMsg));
        } else {
            console.warn("PubSub not initialized yet");
        }
    });

}