import { createLibp2p } from 'libp2p'
import { webSockets } from '@libp2p/websockets'
import { noise } from '@chainsafe/libp2p-noise'
import { yamux } from '@chainsafe/libp2p-yamux'
import { gossipsub } from '@chainsafe/libp2p-gossipsub'
import { circuitRelayServer } from '@libp2p/circuit-relay-v2'
import { identify } from '@libp2p/identify'

import fs from 'fs'
import { exec } from 'child_process';

console.log("===⚡relay.js⚡===")

const port = process.env.PORT || 9090
const listenAddr = `/ip4/0.0.0.0/tcp/${port}/ws`

/**
 * Relay node that handles peers connecting to each other the first time.
 * Hands them off to webRTC once the handshake completes.
 * At that point the connections are handled by the peers' nodes. In this case, whats in /tp-frontend/src/JSPorts/RoomPubsub/room-pubsub.js
 */
const node = await createLibp2p({
  transports: [webSockets()],
  connectionEncryption: [noise()],
  streamMuxers: [yamux()],
  services: {
    identify: identify(),
    pubsub: gossipsub(),
    relay: circuitRelayServer({ reservations: { maxReservations: Infinity } })
  },
  addresses: {
    listen: [listenAddr]
  }
})

const nodePeerId = node.peerId.toString()
console.log(`🔨 Relay node created. ID: ${nodePeerId}`)
console.log(`🔌 Relay node listening on:\n${node.getMultiaddrs().map(addr => addr.toString()).join('\n')}`)

// Write the node's multiaddr to a local file so we can publish to ipfs
for (const addr of node.getMultiaddrs()) {
  const fullAddr = `${addr.toString()}/p2p/${nodePeerId}`

  // Write the first non-localhost address to file
  if (!fullAddr.includes('127.0.0.1')) {
    fs.writeFileSync('tp-relay-ipfs/relay-addr.txt', fullAddr)
    break;
  }
}

// Print the contents of the file
const fileContents = fs.readFileSync('tp-relay-ipfs/relay-addr.txt', 'utf8')
console.log(`📄 Relay node's multiaddr written to file:\n${fileContents}`)

// Call the publish-relay.sh script to publish this addr to ipfs
console.log(`📡 Calling publish-relay.sh script...`);
let fail = false
exec('bash ./publish-relay.sh', (error, stdout, stderr) => {

  console.log(`📡 Relay publish script output:\n"""\n${stdout}\n"""`);

  if (stderr) {
    console.error(`⚠️ Script stderr: ${stderr}`);
  }

  if (error) {
    console.error(`❌ Error running publish script: ${error.name}: ${error.message}`);
    fail = true;
    return;
  }
});

if (fail) {
  console.error(`❌ Failed to publish relay address to ipfs. Exiting...`);
  process.exit(1);
}