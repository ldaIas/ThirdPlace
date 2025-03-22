import * as crypto from '@libp2p/crypto';

// Generate a DID and private key pair
async function generateDID() {
    try {
        const key = await crypto.keys.generateKeyPair('Ed25519');
        const privKey = (await key.export()).toString('base64');
        const pubKey = (await key.public.export()).toString('base64');
        
        const did = `did:key:z${pubKey}`; // Simple DID encoding
        
        // Send result to Elm
        app.ports.didGenerated.send({ did, privKey, pubKey });
    } catch (error) {
        console.error("DID generation failed:", error);
    }
}

// Listen for Elm's request to generate a DID
app.ports.generateDID.subscribe(() => {
    generateDID();
});
