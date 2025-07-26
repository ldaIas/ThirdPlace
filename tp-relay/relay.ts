import { createLibp2p } from 'libp2p'
import * as keys from '@libp2p/crypto/keys'
import { webSockets } from '@libp2p/websockets'
import { noise } from '@chainsafe/libp2p-noise'
import { yamux } from '@chainsafe/libp2p-yamux'
import { gossipsub } from '@chainsafe/libp2p-gossipsub'
import { circuitRelayServer } from '@libp2p/circuit-relay-v2'
import { identify } from '@libp2p/identify'
import { createHelia } from 'helia'
import { LevelDatastore } from 'datastore-level'
import { MemoryBlockstore } from 'blockstore-core'
import path from 'path'
import { fileURLToPath } from 'url'
import { dirname } from 'path'
import { unixfs } from '@helia/unixfs'
import { ipns } from '@helia/ipns'
import { fromString, toString } from 'uint8arrays'
import fs from 'fs/promises'
import { PrivateKey } from '@libp2p/interface'
import { kadDHT } from '@libp2p/kad-dht'


console.log("===⚡relay.ts⚡===")

const port = process.env.PORT || 9090
const listenAddr = `/ip4/0.0.0.0/tcp/${port}/ws`

/**
 * Relay node that handles peers connecting to each other the first time.
 * Hands them off to webRTC once the handshake completes.
 * At that point the connections are handled by the peers' nodes. In this case, whats in /tp-frontend/src/JSPorts/RoomPubsub/room-pubsub.js
 */
const node = await createLibp2p({
  transports: [webSockets()],
  connectionEncrypters: [noise()],
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

/**
 * The rest of this file is dealing with uploading the relay node address to IPFS
 */

// Write the node's multiaddr to a local file so we can publish to ipfs
let fullAddrForIPFS = ""
for (const addr of node.getMultiaddrs()) {
  const fullAddr = `${addr.toString()}/p2p/${nodePeerId}`

  // Write the first non-localhost address to file
  if (!fullAddr.includes('127.0.0.1')) {
    fullAddrForIPFS = fullAddr
    break
  }
}

console.log(`📝 Writing multiaddr to Helia/IPFS: ${fullAddrForIPFS}`)

// Create a file for persistent data storage for Helia
// On Akash this is used with their Persistent Storage to keep the ipfs identity consistent 

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const datastorePath = path.resolve(__dirname, '.tp-data')
const datastore = new LevelDatastore(datastorePath)
const blockstore = new MemoryBlockstore()

const helia = await createHelia({
  datastore,
  blockstore
})
console.log('🧿 Connected peers:', (await helia.libp2p.peerStore.all()).length);
const ufs = unixfs(helia)
const myIpns = ipns(helia)

// Save to IPFS
const cid = await ufs.addBytes(fromString(fullAddrForIPFS))
console.log('✅ relay address CID:', cid.toString())

// Publish to IPNS. In order to do so, we need the key for the IPFS account we are uploading it to
// Need to have done `$ipfs key export <KEY_NAME> -o="tp-relay.ipfs.pvk"`
console.log('🔑 Importing IPFS key...')
let importedKey: PrivateKey
try {
  const base64Text = await fs.readFile('./tp-relay.ipfs.pvk', 'base64')
  const keyBytes = fromString(base64Text.trim(), 'base64')

  importedKey = keys.privateKeyFromProtobuf(keyBytes)
  console.log('🔑 Imported IPFS key:', importedKey.publicKey)
} catch (error) {
  console.log('❌ Error importing IPFS key:', error)
  console.log('🔑 Generating new IPFS key...')
  importedKey = await keys.generateKeyPair("Ed25519")
}

console.log('🛜Publishing to IPNS...')
await myIpns.publish(importedKey, cid)
const ipnsId = importedKey.publicKey
console.log(`📡 Published IPNS address: /ipns/${ipnsId}`)
