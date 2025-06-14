#!/bin/bash

echo "===⚡build_and_run.sh⚡==="

set -euo pipefail

CONTAINER_NAME="tp-relay-container"

# Ensure we're in the project root
cd "$TP_RELAY_ROOT" || exit 1

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
  echo "❌ Docker doesn't seem to be running. Please start Docker Desktop and try again."
  exit 1
fi

# Clean up old container(s)
echo "🧹 Cleaning up old Docker containers/images…"

# 1. Stop any running container with that name (ignore errors if none)
docker stop "$CONTAINER_NAME" 2>/dev/null || true

# 2. Remove any stopped container with that name
docker rm "$CONTAINER_NAME" 2>/dev/null || true

# Remove the old image, forcing a fresh rebuild
docker image rm "tp-relay-image" 2>/dev/null || true

echo "✅ Cleanup done."

# Clean the publish-relay.sh script from /r endings
sed -i 's/\r$//' publish-relay.sh
chmod +x ./publish-relay.sh

# Build and run the container
echo "✅ Docker is running. Building image..."
docker build -t tp-relay-image . &&
docker run --name "$CONTAINER_NAME" --rm -p 9090:9090 tp-relay-image
