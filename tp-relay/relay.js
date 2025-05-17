import { createLibp2p } from 'libp2p'
import { webSockets } from '@libp2p/websockets'
import { webRTC } from '@libp2p/webrtc'
import { noise } from '@chainsafe/libp2p-noise'
import { yamux } from '@chainsafe/libp2p-yamux'
import { gossipsub } from '@chainsafe/libp2p-gossipsub'
import { circuitRelayServer } from '@libp2p/circuit-relay-v2'

const port = process.env.PORT || 9090
const listenAddr = `/ip4/0.0.0.0/tcp/${port}/ws`

/**
 * Relay node that handles peers connecting to each other the first time.
 * Hands them off to webRTC once the handshake completes.
 * At that point the connections are handled by the peers' nodes. In this case, whats in /tp-frontend/src/JSPorts/RoomPubsub/room-pubsub.js
 */
const node = await createLibp2p({
  transports: [webSockets(), webRTC()],
  connectionEncryption: [noise()],
  streamMuxers: [yamux()],
  services: {
    pubsub: gossipsub(),
    relay: circuitRelayServer()
  },
  addresses: {
    listen: [listenAddr]
  }
})

await node.start()
console.log(`Relay node listening on:\n${node.getMultiaddrs().map(addr => addr.toString()).join('\n')}`)