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
      const db = await this.manager.orbitdb.open(dbName, { 
        type: 'documents',
        sync: false // Disable sync to avoid pubsub issues
      });
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

  async createActivityPostsDatabase(): Promise<string | null> {
    if (!this.manager.orbitdb) {
      console.error('OrbitDB not initialized');
      return null;
    }

    try {
      const dbName = 'thirdplace-posts';
      const db = await this.manager.orbitdb.open(dbName, { 
        type: 'documents',
        sync: false // Disable sync to avoid pubsub issues
      });
      this.manager.databases.set(dbName, db);
      
      console.log('Activity posts database created:', db.address);
      return db.address;
    } catch (error) {
      console.error('Failed to create activity posts database:', error);
      return null;
    }
  }

  async submitActivityPost(postData: any): Promise<string | null> {
    // Ensure posts database exists
    let db = this.manager.databases.get('thirdplace-posts');
    if (!db) {
      const address = await this.createActivityPostsDatabase();
      if (!address) {
        return null;
      }
      db = this.manager.databases.get('thirdplace-posts');
    }

    if (!db) {
      console.error('Activity posts database not available');
      return null;
    }

    try {
      // Generate a unique ID and add metadata
      const activityPost = {
        _id: Date.now().toString(),
        id: Date.now().toString(),
        ...postData,
        author: 'user_' + Math.random().toString(36).substr(2, 9), // Temporary user ID
        createdAt: new Date().toISOString(),
        endDate: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 24 hours from now
        proposedTime: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(), // 2 hours from now
        status: 'active',
        genderBalance: 'any',
        // Mock coordinates for San Francisco
        latitude: 37.7749 + (Math.random() - 0.5) * 0.01,
        longitude: -122.4194 + (Math.random() - 0.5) * 0.01,
        geohash: '9q8yy' // Mock geohash for SF area
      };

      const hash = await db.put(activityPost);
      console.log('Activity post submitted with hash:', hash);
      console.log('Post data:', activityPost);
      return hash;
    } catch (error) {
      console.error('Failed to submit activity post:', error);
      return null;
    }
  }

  async getAllActivityPosts(): Promise<any[] | null> {
    const db = this.manager.databases.get('thirdplace-posts');
    if (!db) {
      console.error('Activity posts database not found');
      return null;
    }

    try {
      const allPosts = await db.all();
      console.log('Retrieved all activity posts:', allPosts);
      return allPosts;
    } catch (error) {
      console.error('Failed to retrieve activity posts:', error);
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