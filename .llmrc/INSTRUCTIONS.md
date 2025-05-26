# Instructions for LLMs

## Introduction

We are creating a website called ThirdPlace that acts as a third place. Users are able to interact
with others in rooms, which are geolocked. The main goal of the website/app is to get people to make
real life connections through interactions on the app. It is meant to be completely free and fair to all users.

One of the main aspects of ThirdPlace is its decentralization. It is meant to be able to be reached from anywhere
and anyone can host a service.

## Project stack

- Elm for the webpage model
- JS to integrate with LibP2P for WebRTC P2P uses
- Node.js to host a LibP2P WebRTC "bootstrap" server. Hosted by an Akash node. See more in /tp-relay/README.md

## Special Instructions

- Save user and project preferences to MEMORIES.md
- Be conscious of response sizes; try to be efficient and concise
- Each sub-project (frontend, server) has their own .llmrc/ (large language model resource) directory to reference for
    specific instructions for that project.
