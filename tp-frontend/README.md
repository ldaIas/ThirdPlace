
# ThirdPlace Frontend

A decentralized social application frontend designed to foster conversation and community. The frontend is a single-page application focused on simplicity, anonymity, and resilience, served entirely over IPFS.

## Overview

ThirdPlace is a decentralized social app that enables real-time P2P chat through WebRTC, with web3 identity authentication via KILT blockchain. The application prioritizes user privacy and decentralized infrastructure.

## Technologies

- **Elm** – Main frontend language (pure functional, type-safe)
  - Ports – Used for JavaScript interop (LibP2P, KILT integration)
- **IPFS** – Hosts and serves the static frontend (fully decentralized)
- **LibP2P (via JS)** – Enables real-time P2P chat via WebRTC
- **IndexedDB / localStorage** – Client-side state persistence
- **Polkadot/KILT blockchain** – Web3 identities for authentication
- **Vite** – Development server and build tooling

## Project Structure

```
tp-frontend/
├── src/
│   ├── JSPorts/              # JavaScript interop ports
│   │   ├── Identity/         # KILT identity management
│   │   ├── Sporran/          # Sporran wallet integration
│   │   ├── Geohash/          # Location-based room discovery
│   │   ├── RoomPubsub/       # Room messaging system
│   │   └── WebRTC/           # P2P communication
│   ├── Views/                # UI components
│   │   ├── Login/            # Authentication interface
│   │   ├── Room/             # Chat room interface
│   │   └── ThirdPlaceAppView.elm
│   ├── Utils/                # Utility functions
│   ├── ThirdPlaceApp.elm     # Main application entry point
│   └── ThirdPlaceModel.elm   # Application state model
├── stylesheets/              # CSS styling
├── index.html               # HTML entry point
├── build.sh                 # Elm compilation script
├── serve.sh                 # Development server script
└── package.json             # Node.js dependencies
```

## Development Setup

### Prerequisites

- [Elm](https://guide.elm-lang.org/install/elm.html)
- Node.js and npm
- Modern web browser with WebRTC support

### Installation

1. Install dependencies:
   ```bash
   npm install
   ```

2. Build the application:
   ```bash
   npm run build
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

The application will be available at `http://localhost:5173/`

## Architecture

### Elm Application
The main application is written in Elm (`ThirdPlaceApp.elm`) using The Elm Architecture (TEA) pattern with:
- **Model** – Application state
- **Update** – State transitions
- **View** – UI rendering
- **Subscriptions** – External event handling

### JavaScript Ports
JavaScript integration is handled through Elm ports in the `JSPorts/` directory:
- **Identity** – KILT DID generation and authentication
- **Sporran** – Wallet connectivity
- **WebRTC** – P2P communication setup
- **Geohash** – Location-based room discovery
- **RoomPubsub** – Real-time messaging

### UI Components
- **Login View** – User authentication interface
- **Room View** – Main chat interface with conversation panel and chat panel
- **Chat Panel** – Message display and input
- **Conversations** – Room/conversation management

## Deployment

_This is a manual process right now, but we hope to make this automatic._
The application is designed for decentralized deployment via IPFS:

1. Build the application:
   ```bash
   $ npm run build
   ```

2. Collect all assets (HTML, CSS, JS, compiled Elm) into a deployment folder

3. Upload to IPFS for decentralized hosting

Users access the application via IPFS URLs or HTTP gateways rather than traditional domain names.

## Features

- **Decentralized Identity** – KILT blockchain-based authentication
- **P2P Communication** – Direct WebRTC connections for chat
- **Location-based Rooms** – Geohash-based room discovery
- **Privacy-focused** – No central servers for messaging
- **Anonymous by Design** – Optional identity disclosure
- **IPFS Hosting** – Fully decentralized application delivery

## Development Commands

- `npm run dev` – Start development server (port 5173)
- `npm run build` – Build all necessary artifacts: Elm, TypeScript, then bundle with vite

## License

GNU AFFERO GENERAL PUBLIC LICENSE Version 3 - See [LICENSE](../LICENSE)
