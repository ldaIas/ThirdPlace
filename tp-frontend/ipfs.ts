import { createHelia, type Helia } from 'helia';
import { json, type JSON } from '@helia/json';
import { CID } from 'multiformats/cid';
import { createLibp2p } from 'libp2p';
import { gossipsub } from '@chainsafe/libp2p-gossipsub';
import { noise } from '@chainsafe/libp2p-noise';
import { yamux } from '@chainsafe/libp2p-yamux';
import { bootstrap } from '@libp2p/bootstrap';
import { webSockets } from '@libp2p/websockets';
import { webRTC } from '@libp2p/webrtc';
import { identify } from '@libp2p/identify';
import { circuitRelayTransport } from '@libp2p/circuit-relay-v2';

interface IPFSManager {
  helia: Helia | null;
  peerCount: number;
  status: string;
}

class IPFSService {
  private manager: IPFSManager = {
    helia: null,
    peerCount: 0,
    status: 'Connecting...'
  };

  private statusCallback: ((status: string, peerCount: number) => void) | null = null;

  async initialize(): Promise<void> {
    try {
      const libp2p = await createLibp2p({
        transports: [
          webSockets(),
          webRTC(),
          circuitRelayTransport()
        ],
        connectionEncrypters: [noise()],
        streamMuxers: [yamux()],
        peerDiscovery: [
          bootstrap({
            list: [
              '/dnsaddr/bootstrap.libp2p.io/p2p/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN',
              '/dnsaddr/bootstrap.libp2p.io/p2p/QmQCU2EcMqAqQPR2i9bChDtGNJchTbq5TbXJJ16u19uLTa'
            ]
          })
        ],
        services: {
          identify: identify(),
          pubsub: gossipsub({
            allowPublishToZeroTopicPeers: true
          })
        }
      });

      this.manager.helia = await createHelia({ libp2p });
      this.manager.status = 'Connected to IPFS';
      
      // Set up peer count monitoring
      this.setupPeerMonitoring();
      
      this.updateStatus();
    } catch (error) {
      console.error('Failed to initialize Helia:', error);
      this.manager.status = 'Connection failed';
      this.updateStatus();
    }
  }

  private setupPeerMonitoring(): void {
    if (!this.manager.helia) return;

    const updatePeerCount = () => {
      if (this.manager.helia) {
        const peers = this.manager.helia.libp2p.getPeers();
        this.manager.peerCount = peers.length;
        this.updateStatus();
      }
    };

    // Update peer count immediately
    updatePeerCount();

    // Monitor peer connections
    this.manager.helia.libp2p.addEventListener('peer:connect', updatePeerCount);
    this.manager.helia.libp2p.addEventListener('peer:disconnect', updatePeerCount);

    // Periodic update
    setInterval(updatePeerCount, 5000);
  }

  private updateStatus(): void {
    if (this.statusCallback) {
      const statusText = this.manager.status === 'Connected to IPFS' 
        ? `Connected to IPFS (${this.manager.peerCount} peers)`
        : this.manager.status;
      
      this.statusCallback(statusText, this.manager.peerCount);
    }
  }

  onStatusChange(callback: (status: string, peerCount: number) => void): void {
    this.statusCallback = callback;
  }

  getPeerCount(): number {
    return this.manager.peerCount;
  }

  getStatus(): string {
    return this.manager.status;
  }

  getHelia(): Helia | null {
    return this.manager.helia;
  }

  async publishTestContent(): Promise<string | null> {
    if (!this.manager.helia) {
      console.error('Helia not initialized');
      return null;
    }

    try {
      const j = json(this.manager.helia);
      const testData = {
        message: 'Hello from ThirdPlace!',
        timestamp: new Date().toISOString(),
        type: 'test-content'
      };
      
      const cid = await j.add(testData);
      console.log('Published test content with CID:', cid.toString());
      return cid.toString();
    } catch (error) {
      console.error('Failed to publish test content:', error);
      return null;
    }
  }

  async retrieveContent(cidString: string): Promise<any | null> {
    if (!this.manager.helia) {
      console.error('Helia not initialized');
      return null;
    }

    try {
      const j = json(this.manager.helia);
      const cid = CID.parse(cidString);
      const content = await j.get(cid);
      console.log('Retrieved content:', content);
      return content;
    } catch (error) {
      console.error('Failed to retrieve content:', error);
      return null;
    }
  }

  async stop(): Promise<void> {
    if (this.manager.helia) {
      await this.manager.helia.stop();
      this.manager.helia = null;
      this.manager.peerCount = 0;
      this.manager.status = 'Disconnected';
      this.updateStatus();
    }
  }
}

export const ipfsService = new IPFSService();