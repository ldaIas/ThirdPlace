#!/bin/bash

# Ensure we're in the project root
cd "$TP_RELAY_ROOT" || exit 1

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
  echo "❌ Docker doesn't seem to be running. Please start Docker Desktop and try again."
  exit 1
fi

# Build and run the container
echo "✅ Docker is running. Building image..."
docker build -t tp-relay . &&
docker run -p 9090:9090 tp-relay
