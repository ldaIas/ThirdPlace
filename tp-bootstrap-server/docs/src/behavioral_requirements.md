# Behavioral Requirements 
The goal of this document is to give an overview of the behaviors we are expecting out of a decentralized backend.

## Resources
This section will keep helpful resources that we found useful in researching topics discussed in this document. 

- [Rust libp2p Tutorials](https://docs.rs/libp2p/latest/libp2p/tutorials/index.html)
- [libp2p Circuit Relay](https://docs.libp2p.io/concepts/nat/circuit-relay/)


## Big Picture 
This section will contain `Big Picture` items that we will organize in MoSCoW format in order to give future readers a general understanding of what this backend implementation is trying to achieve at the highest level.

### Must Have
- Negotiate connections between peers
- Have a way for peers connecting on decentralized network to find backend
- Ability for users to easily spin up their own nodes


### Should Have
- Implementation in Rust
- WebRTC connection between peers

### Could Have
- Have static ip
- Libp2p Circuit relay
- Stun server
- Turn server
- Signaling Server


### Wont Have
- Centralized server that contains user data beyond connectivity info
- Usage of HTTPS 





## Goals 4/31/2025
This section will give goals to meet through the end of April:

- Basic startup happens and RTC connections can happen. This will allow us to interact with the server and further development can happen on the IPFS front after that.

