import { keys } from "https://esm.sh/@libp2p/crypto";

function toBase64(uint8Array) {
    return btoa(String.fromCharCode(...uint8Array));
}

// Generate a DID and private key pair
async function generateDID() {
    try {

        // Key shape: { type: "Ed25519", raw: Uint8Array, publicKey: { raw: Uint8Array, type: "Ed25519" } }
        const key = await keys.generateKeyPair("Ed25519");
        
        const privKey = toBase64(key.raw).toString('base64');
        const pubKey = toBase64(key.publicKey.raw).toString('base64');
    
        const did = `did:key:z${pubKey}`; // Simple DID encoding
        
        // Send result to Elm
        app.ports.didGenerated.send({ did, privKey, pubKey });
    } catch (error) {
        console.error("DID generation failed:", error);
    }
}

export { generateDID };

// Listen for Elm's request to generate a DID
app.ports.generateDID.subscribe(() => {
    generateDID();
});

async function authenticate(did, privKey) {
    try {

        const privKeyBytes = Uint8Array.from(atob(privKey), c => c.charCodeAt(0));
        const key = keys.privateKeyFromRaw(privKeyBytes);

        console.log(key)
        const message = new TextEncoder().encode("auth-test");
        const signature = await key.sign(message);
        const isValid = await key.publicKey.verify(message, signature);
        return isValid;
    } catch (error) {
        console.error("Authentication failed:", error);
        return false;
    }
}

export { authenticate };

app.ports.authenticate.subscribe(({ did, privKey }) => {
    authenticate(did, privKey).then(isValid => {
        app.ports.authenticationResult.send(isValid);
    });
});