import * as did from '@kiltprotocol/did';
console.log(did);
import { web3Enable, web3Accounts, web3FromSource } from '@polkadot/extension-dapp';
import { connect } from '@kiltprotocol/core';

export function setupSporranPorts(app) {
  app.ports.detectSporran.subscribe(async () => {
    const extensions = await web3Enable("ThirdPlace");
    if (extensions.length === 0) {
      app.ports.onSporranDetected.send(false);
    } else {
      app.ports.onSporranDetected.send(true);
    }
  });

  app.ports.requestLogin.subscribe(async () => {
    await connect(); // connect to a KILT node (default dev chain is fine for now)

    const accounts = await web3Accounts();
    if (accounts.length === 0) {
      console.warn("No accounts in Sporran");
      return;
    }

    const account = accounts[0]; // Use first for now
    const didDoc = await getSelectedDid();
    if (didDoc) {
      app.ports.onLoginSuccess.send(didDoc.uri);
    } else {
      console.warn("No DID found for account");
    }
  });
}
