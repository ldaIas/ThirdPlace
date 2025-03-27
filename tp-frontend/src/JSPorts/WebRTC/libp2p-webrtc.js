import { createLibp2p } from 'libp2p'
import { noise } from '@chainsafe/libp2p-noise'
import { webRTC } from '@libp2p/webrtc'
import { kadDHT, removePrivateAddressesMapper } from '@libp2p/kad-dht'
import { multiaddr } from 'multiaddr'
import { peerIdFromString } from '@libp2p/peer-id'

class Libp2pManager {
    constructor(app) {
        this.app = app
        this.node = null
        this.setupPorts()
    }

    setupPorts() {
        // Elm to JS communication ports
        if (this.app.ports) {
            this.app.ports.createConnection.subscribe(this.initializeNode.bind(this))
            this.app.ports.sendOffer.subscribe(this.handleSendOffer.bind(this))
            this.app.ports.sendAnswer.subscribe(this.handleSendAnswer.bind(this))
            this.app.ports.sendCandidate.subscribe(this.handleSendCandidate.bind(this))
        }
    }

    async initializeNode(config = {}) {
        try {
            this.node = await createLibp2p({
                transports: [webRTC()],
                connectionEncryption: [noise()],
                peerDiscovery: [
                    kadDHT({
                        protocol: '/ipfs/kad/1.0.0',
                        peerInfoMapper: removePrivateAddressesMapper
                    })
                ],
                services: {
                    // DHT configuration
                    dht: kadDHT({
                        protocol: '/ipfs/kad/1.0.0',
                        peerInfoMapper: removePrivateAddressesMapper
                    })
                },
                ...config
            })

            // Set up event listeners
            this.setupNodeEventListeners()

            // Notify Elm that node is ready
            if (this.app.ports.nodeInitialized) {
                this.app.ports.nodeInitialized.send({
                    peerId: this.node.peerId.toString(),
                    multiaddrs: this.node.getMultiaddrs().map(ma => ma.toString())
                })
            }

            return this.node
        } catch (error) {
            console.error('Node initialization failed:', error)
            
            // Send error to Elm
            if (this.app.ports.nodeInitializationError) {
                this.app.ports.nodeInitializationError.send(error.message)
            }
        }
    }

    setupNodeEventListeners() {
        // WebRTC specific event listeners
        this.node.addEventListener('peer:connect', (event) => {
            if (this.app.ports.peerConnected) {
                this.app.ports.peerConnected.send({
                    peerId: event.detail.peerId.toString()
                })
            }
        })

        this.node.addEventListener('peer:disconnect', (event) => {
            if (this.app.ports.peerDisconnected) {
                this.app.ports.peerDisconnected.send({
                    peerId: event.detail.peerId.toString()
                })
            }
        })

        // WebRTC signaling event handlers
        this.node.transportManager.addEventListener('webrtc:signaling:offer', (event) => {
            if (this.app.ports.receiveOffer) {
                this.app.ports.receiveOffer.send({
                    offer: event.detail.offer,
                    peerId: event.detail.peerId.toString()
                })
            }
        })

        this.node.transportManager.addEventListener('webrtc:signaling:answer', (event) => {
            if (this.app.ports.receiveAnswer) {
                this.app.ports.receiveAnswer.send({
                    answer: event.detail.answer,
                    peerId: event.detail.peerId.toString()
                })
            }
        })
    }

    async handleSendOffer(offerData) {
        try {
            const remotePeerId = peerIdFromString(offerData.peerId)
            
            // Establish connection and send offer
            const connection = await this.node.dial(remotePeerId)
            
            // Send WebRTC offer
            await connection.newStream('/webrtc/offer/1.0.0')
            
            if (this.app.ports.offerSent) {
                this.app.ports.offerSent.send({ success: true })
            }
        } catch (error) {
            console.error('Offer sending failed:', error)
            
            if (this.app.ports.offerSendError) {
                this.app.ports.offerSendError.send(error.message)
            }
        }
    }

    async handleSendAnswer(answerData) {
        try {
            const remotePeerId = peerIdFromString(answerData.peerId)
            
            // Establish connection and send answer
            const connection = await this.node.dial(remotePeerId)
            
            // Send WebRTC answer
            await connection.newStream('/webrtc/answer/1.0.0')
            
            if (this.app.ports.answerSent) {
                this.app.ports.answerSent.send({ success: true })
            }
        } catch (error) {
            console.error('Answer sending failed:', error)
            
            if (this.app.ports.answerSendError) {
                this.app.ports.answerSendError.send(error.message)
            }
        }
    }

    async handleSendCandidate(candidateData) {
        try {
            const remotePeerId = peerIdFromString(candidateData.peerId)
            
            // Establish connection and send ICE candidate
            const connection = await this.node.dial(remotePeerId)
            
            // Send ICE candidate
            await connection.newStream('/webrtc/candidate/1.0.0')
            
            if (this.app.ports.candidateSent) {
                this.app.ports.candidateSent.send({ success: true })
            }
        } catch (error) {
            console.error('Candidate sending failed:', error)
            
            if (this.app.ports.candidateSendError) {
                this.app.ports.candidateSendError.send(error.message)
            }
        }
    }

    // Additional utility methods
    async discoverPeers() {
        if (!this.node) {
            throw new Error('Node not initialized')
        }

        const peers = await this.node.dht.findPeers()
        
        if (this.app.ports.peersDiscovered) {
            this.app.ports.peersDiscovered.send(
                peers.map(peer => peer.id.toString())
            )
        }
    }
}

// Initialize the manager when the app loads
export function initLibp2p(app) {
    return new Libp2pManager(app)
}