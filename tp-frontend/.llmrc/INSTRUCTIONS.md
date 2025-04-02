# Instructions for LLMs

## Introduction

We are in the frontend project root directory.
The frontend page model is written in and controlled by the Elm files.
The Elm files have ports for integrating with the Libp2p javascript library.
The port definition and implementations are in the JSPorts/ directory.

## Deployment

With decentralization at the core of the whole project, the webpage will be served via IPFS (InterPlanetary File System)
rather than from some host machine. Thus there is also no domain name.

The webpage is an SPA elm app, so we just need to serve a single index.html file that has the necessary javascript resources.
The elm compiles to a js file (currently tpapp.js). We take this, the stylesheets, other resources, and the javascript files from JSPorts,
and put them in a folder named "deploy." This directory is then served to IPFS.
Currently this is a manual process and the file is uploaded via the IPFS desktop client.
