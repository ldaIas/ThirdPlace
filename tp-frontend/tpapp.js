import { ipfsService } from './ipfs.ts';
import { orbitDBService } from './orbitdb.ts';

// Wait for Elm to be available and initialize the application
function initializeApp() {
    if (typeof window.Elm === 'undefined') {
        // Wait a bit more for Elm to load
        setTimeout(initializeApp, 100);
        return;
    }

    const app = window.Elm.UI.Main.init({
        node: document.getElementById('app'),
        flags: {}
    });
    
    console.log('ThirdPlace app initialized');
    
    // Set up IPFS status callback
    ipfsService.onStatusChange((status, peerCount) => {
        if (app.ports && app.ports.ipfsStatusChanged) {
            app.ports.ipfsStatusChanged.send(status);
        }
    });
    
    // Set up OrbitDB status callback
    orbitDBService.onStatusChange((status) => {
        if (app.ports && app.ports.orbitDBStatusChanged) {
            app.ports.orbitDBStatusChanged.send(status);
        }
    });
    
    // Set up publish test content handler
    if (app.ports && app.ports.publishTestContent) {
        app.ports.publishTestContent.subscribe(async () => {
            const cid = await ipfsService.publishTestContent();
            if (cid && app.ports.contentPublished) {
                app.ports.contentPublished.send(cid);
                
                // Automatically retrieve the content to verify
                setTimeout(async () => {
                    console.log("Attempting to retrieve content...")
                    const content = await ipfsService.retrieveContent(cid);
                    if (content && app.ports.contentRetrieved) {
                        app.ports.contentRetrieved.send(JSON.stringify(content));
                    }
                }, 1000);
            }
        });
    }
    
    // Set up OrbitDB test handlers
    if (app.ports && app.ports.createTestDatabase) {
        app.ports.createTestDatabase.subscribe(async () => {
            const address = await orbitDBService.createTestDatabase();
            if (address && app.ports.databaseCreated) {
                app.ports.databaseCreated.send(address);
            }
        });
    }
    
    if (app.ports && app.ports.addTestData) {
        app.ports.addTestData.subscribe(async () => {
            const hash = await orbitDBService.addTestData();
            if (hash && app.ports.dataAdded) {
                app.ports.dataAdded.send(hash);
            }
        });
    }
    
    if (app.ports && app.ports.retrieveAllData) {
        app.ports.retrieveAllData.subscribe(async () => {
            const data = await orbitDBService.getAllTestData();
            if (data && app.ports.allDataRetrieved) {
                app.ports.allDataRetrieved.send(JSON.stringify(data));
            }
        });
    }
    
    // Initialize IPFS
    ipfsService.initialize().then(() => {
        console.log('IPFS service initialized');
        
        // Initialize OrbitDB after IPFS is ready
        const helia = ipfsService.getHelia();
        if (helia) {
            orbitDBService.initialize(helia).then(() => {
                console.log('OrbitDB service initialized');
            }).catch((error) => {
                console.error('Failed to initialize OrbitDB service:', error);
            });
        }
    }).catch((error) => {
        console.error('Failed to initialize IPFS service:', error);
    });
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', initializeApp);