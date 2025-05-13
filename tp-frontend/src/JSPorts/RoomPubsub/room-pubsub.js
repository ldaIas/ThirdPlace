import { createLibp2p } from 'libp2p';
import { webSockets } from '@libp2p/websockets';
import { noise } from '@chainsafe/libp2p-noise';
import { yamux } from '@chainsafe/libp2p-yamux';
import { gossipsub } from '@chainsafe/libp2p-gossipsub';
import { identify } from '@libp2p/identify';

let node;
let topic;

export async function setupRoomPubSubPorts(app) {

    app.ports.joinRoom.subscribe(async (roomId) => {
        console.log("Joining room", roomId);
        topic = `tp-${roomId}`;
        node = await createLibp2p({
            transports: [webSockets()],
            connectionEncryption: [noise()],
            streamMuxers: [yamux()],
            services: {
                pubsub: gossipsub(),
                identify: identify(),
            }
        });

        node.addEventListener('peer:connect', (event) => {
            console.log("connection established to ", event.detail.remotePeer.toString());
        });

        await node.start();
        console.log(`Node started. Subscribing to ${topic}`);
        console.log('node', node)

        const gossipService = gossipsub;
        console.log("gossipsub() returns:", gossipService);
        console.log("pubsub", node.pubsub)
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
                console.error("Invalid message received:", messageJson)
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
            await node.services.pubsub.publish(topic, new TextEncoder().encode(jsonMsg));
        } else {
            console.warn("PubSub not initialized yet");
        }
    });

}