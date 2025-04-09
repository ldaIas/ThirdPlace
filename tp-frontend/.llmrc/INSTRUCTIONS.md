# ThirdPlace Frontend Overview

## Introduction

ThirdPlace is a decentralized social app designed to foster conversation and community. The frontend is a single-page application focused on simplicity, anonymity, and resilience, served entirely over IPFS.

We are in the frontend project root directory.
The frontend page model is written in and controlled by the Elm files.
The Elm files have ports for integrating with the Libp2p and KILT javascript libraries.
The port definition and implementations are in the JSPorts/ directory.

## Technologies

- Elm – Main frontend language (pure functional, type-safe)
  - Ports – Used to interop with JavaScript (e.g., LibP2P bindings)
- IPFS – Used to host and serve the static frontend (fully decentralized)
- LibP2P (via JS) – Enables real-time P2P chat via WebRTC
- IndexedDB / localStorage – For client-side state persistence
- Polkadot/KILT blockhains - web3 identities for authentication

## Deployment

With decentralization at the core of the whole project, the webpage will be served via IPFS (InterPlanetary File System)
rather than from some host machine. Thus there is also no domain name. End users will reach the app via the ipfs url or http gateway.

The webpage is an SPA elm app, so we just need to serve a single index.html file that has the necessary javascript resources.
The elm compiles to a js file (currently tpapp.js). We take this, the stylesheets, other resources, and the javascript files from JSPorts,
and put them in a folder named "deploy." This directory is then served to IPFS.
Currently this is a manual process and the file is uploaded via the IPFS desktop client.
