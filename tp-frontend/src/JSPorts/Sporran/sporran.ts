import { web3Enable, web3Accounts } from '@polkadot/extension-dapp';
import { connect, disconnect } from '@kiltprotocol/core';

import type { App } from '../../types/apptypes'

/**
 * Sets up ports for Sporran wallet integration.
 * Sporran is a browser extension wallet for KILT blockchain identity management.
 * This module handles wallet detection and authentication.
 */
export function setupSporranPorts(app: App): void {
  // Call from Elm to check if Sporran wallet extension is installed and available
  app.ports.detectSporran.subscribe(async () => {
    // Attempt to enable web3 extensions with our app name
    const extensions = await web3Enable("ThirdPlace");
    // Send result back to Elm based on whether extensions were found
    if (extensions.length === 0) {
      app.ports.onSporranDetected.send(false);
    } else {
      app.ports.onSporranDetected.send(true);
    }
  });

  // Handle login requests from the Elm application
  app.ports.requestLogin.subscribe(async () => {

    // Notify Elm that login attempt has started (we use this to display "logging in..." text or similar)
    app.ports.onLoginAttempted.send(null);

    // Connect to KILT blockchain mainnet
    await connect("wss://spiritnet.kilt.io");

    // Retrieve accounts from the Sporran wallet
    const accounts = await web3Accounts();
    console.log("accounts", accounts);
    if (accounts.length === 0) {
      console.warn("No accounts in Sporran");
      return;
    }

    // Use the first account for authentication
    const account = accounts[0];
    console.log("account", account);
    if (account) {
      // Send account details back to Elm on successful login
      app.ports.onLoginSuccess.send("account " + JSON.stringify(account));
    } else {
      console.warn("No DID found for account");
    }

    // Disconnect from the KILT blockchain after authentication
    // NOTE: This will display red text in the console saying something like "Disconnection NORMAL EVENT"
    await disconnect();
  });
}
