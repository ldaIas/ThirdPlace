import { createHelia, type Helia } from 'helia';
import { json, type JSON } from '@helia/json';
import { CID } from 'multiformats/cid';

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

  async initialize(): Promise<Helia> {
    try {
      this.manager.helia = await createHelia();
      this.manager.status = 'Connected to IPFS';
      
      // Set up peer count monitoring
      this.setupPeerMonitoring();
      
      this.updateStatus();
      return this.manager.helia;
    } catch (error) {
      console.error('Failed to initialize Helia:', error);
      this.manager.status = 'Connection failed';
      this.updateStatus();
      throw error;
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