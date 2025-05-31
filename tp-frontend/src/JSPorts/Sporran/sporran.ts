import { web3Enable, web3Accounts, web3FromSource } from '@polkadot/extension-dapp';
import { connect, disconnect } from '@kiltprotocol/core';

import type { App } from '../types/apptypes'

export function setupSporranPorts(app: App): void {
  app.ports.detectSporran.subscribe(async () => {
    const extensions = await web3Enable("ThirdPlace");
    if (extensions.length === 0) {
      app.ports.onSporranDetected.send(false);
    } else {
      app.ports.onSporranDetected.send(true);
    }
  });

  app.ports.requestLogin.subscribe(async () => {
    app.ports.onLoginAttempted.send(null);

    await connect("wss://spiritnet.kilt.io"); // mainnet kilt chain connection

    const accounts = await web3Accounts();
    console.log("accounts", accounts);
    if (accounts.length === 0) {
      console.warn("No accounts in Sporran");
      return;
    }

    const account = accounts[0];
    console.log("account", account);
    if (account) {
      app.ports.onLoginSuccess.send("account " + JSON.stringify(account));
    } else {
      console.warn("No DID found for account");
    }

    await disconnect();
  });
}
