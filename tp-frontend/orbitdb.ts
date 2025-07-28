import { createOrbitDB } from '@orbitdb/core';
import { type Helia } from 'helia';

interface OrbitDBManager {
  orbitdb: any | null;
  databases: Map<string, any>;
  status: string;
}

class OrbitDBService {
  private manager: OrbitDBManager = {
    orbitdb: null,
    databases: new Map(),
    status: 'Not initialized'
  };

  private statusCallback: ((status: string) => void) | null = null;

  async initialize(helia: Helia): Promise<void> {
    try {
      this.manager.status = 'Initializing OrbitDB...';
      this.updateStatus();

      this.manager.orbitdb = await createOrbitDB({ ipfs: helia });
      this.manager.status = 'OrbitDB connected';
      
      this.updateStatus();
      console.log('OrbitDB initialized successfully');
    } catch (error) {
      console.error('Failed to initialize OrbitDB:', error);
      this.manager.status = 'OrbitDB connection failed';
      this.updateStatus();
    }
  }

  private updateStatus(): void {
    if (this.statusCallback) {
      this.statusCallback(this.manager.status);
    }
  }

  onStatusChange(callback: (status: string) => void): void {
    this.statusCallback = callback;
  }

  getStatus(): string {
    return this.manager.status;
  }

  async createTestDatabase(): Promise<string | null> {
    if (!this.manager.orbitdb) {
      console.error('OrbitDB not initialized');
      return null;
    }

    try {
      const dbName = 'thirdplace-test';
      const db = await this.manager.orbitdb.open(dbName, { type: 'documents' });
      this.manager.databases.set(dbName, db);
      
      console.log('Test database created:', db.address);
      return db.address;
    } catch (error) {
      console.error('Failed to create test database:', error);
      return null;
    }
  }

  async addTestData(): Promise<string | null> {
    const db = this.manager.databases.get('thirdplace-test');
    if (!db) {
      console.error('Test database not found');
      return null;
    }

    try {
      const testData = {
        _id: Date.now().toString(),
        message: 'Hello from OrbitDB!',
        timestamp: new Date().toISOString(),
        type: 'test-data'
      };

      const hash = await db.put(testData);
      console.log('Test data added with hash:', hash);
      return hash;
    } catch (error) {
      console.error('Failed to add test data:', error);
      return null;
    }
  }

  async getAllTestData(): Promise<any[] | null> {
    const db = this.manager.databases.get('thirdplace-test');
    if (!db) {
      console.error('Test database not found');
      return null;
    }

    try {
      const allData = await db.all();
      console.log('Retrieved all test data:', allData);
      return allData;
    } catch (error) {
      console.error('Failed to retrieve test data:', error);
      return null;
    }
  }

  async stop(): Promise<void> {
    if (this.manager.orbitdb) {
      for (const [name, db] of this.manager.databases) {
        await db.close();
      }
      this.manager.databases.clear();
      
      await this.manager.orbitdb.stop();
      this.manager.orbitdb = null;
      this.manager.status = 'Disconnected';
      this.updateStatus();
    }
  }
}

export const orbitDBService = new OrbitDBService();