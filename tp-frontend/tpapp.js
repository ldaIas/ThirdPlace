import { ipfsService } from './ipfs.ts';

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
    
    // Set up publish test content handler
    if (app.ports && app.ports.publishTestContent) {
        app.ports.publishTestContent.subscribe(async () => {
            const cid = await ipfsService.publishTestContent();
            if (cid && app.ports.contentPublished) {
                app.ports.contentPublished.send(cid);
                
                // Automatically retrieve the content to verify
                setTimeout(async () => {
                    const content = await ipfsService.retrieveContent(cid);
                    if (content && app.ports.contentRetrieved) {
                        app.ports.contentRetrieved.send(JSON.stringify(content));
                    }
                }, 1000);
            }
        });
    }
    
    // Initialize IPFS
    ipfsService.initialize().then(() => {
        console.log('IPFS service initialized');
    }).catch((error) => {
        console.error('Failed to initialize IPFS service:', error);
    });
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', initializeApp);