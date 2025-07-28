# ThirdPlace

A decentralized social coordination app that helps people find others to join them for real-world activities, fostering authentic connections and combating social isolation.

## The Problem

Singles and small groups often want to engage in social activities (bars, coffee shops, hiking, events) but avoid going alone. Existing solutions like Meetup focus on large recurring groups, while dating apps create artificial high-pressure interactions. Many people end up staying home instead of enjoying activities they'd love to do.

## The Solution

ThirdPlace enables ad-hoc social coordination where users can:

- Propose activities ("Who wants to grab coffee at [Cafe Name] at 2pm?")
- Join others' activity proposals in their local area
- Form small, intimate groups (1-on-1 to small groups of 3-4)
- Connect in low-pressure, activity-focused settings
- Meet people in their actual community/geographic area

## Key Features

- **Activity-focused**: Organize around specific venues and times, not abstract socializing
- **Geographic filtering**: Only see and connect with people in your local community
- **Ephemeral posts**: Activities expire after their event time to keep content fresh
- **Gender balancing**: Optional balanced group formation for more natural social dynamics
- **Direct messaging**: Coordinate details before meeting up

## Technical Architecture

ThirdPlace is built as a decentralized application using:

- **Frontend**: Elm for reliable UI with JavaScript for effects
- **Data Storage**: IPFS for distributed content storage
- **Database**: OrbitDB for stateful data management (posts, RSVPs, messages)
- **Persistence**: Pinning services ensure content availability
- **Infrastructure**: Decentralized backend on Akash Network

### Why Decentralized?

- **User-first design**: No algorithms optimizing for engagement over user success
- **Community ownership**: No central authority controlling social interactions
- **Privacy-focused**: Data distributed across network, not centralized in corporate databases
- **Transparent operations**: Open source and auditable

## Development Status

🚧 **Currently in development** - Building MVP with core activity coordination features.

### Current Phase: IPFS Foundation
Setting up basic distributed content storage and retrieval capabilities.

## Getting Started

*Development setup instructions will be added as the project progresses.*

## Contributing

This project is fully open source. Contributions welcome through:
- Code contributions (PRs)
- Community feedback and testing
- Donations for development and infrastructure

## Vision

ThirdPlace aims to recreate the social coordination that historically happened in physical "third places" (spaces outside work and home) by providing digital tools that facilitate real-world connections rather than replacing them.

The goal is to get people off their phones and into shared activities with their neighbors and community members.
# License
GNU AFFERO GENERAL PUBLIC LICENSE Version 3

See LICENSE
