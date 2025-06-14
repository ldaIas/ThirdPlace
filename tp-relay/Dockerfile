FROM node:20

ENV IPFS_PATH=/app/tp-ipfs

# Install IPFS
RUN curl -O https://dist.ipfs.tech/kubo/v0.25.0/kubo_v0.25.0_linux-amd64.tar.gz && \
    tar -xvzf kubo_v0.25.0_linux-amd64.tar.gz && \
    cd kubo && \
    bash install.sh && \
    echo "ipfs installed:\n" && \
    ipfs --version

WORKDIR /app

# Only copy package files first (for caching)
COPY package.json package-lock.json ./

# Install the dependencies
RUN npm install

# Copy the rest of the code (except whats in .dockerignore)
# If we do this without installing first, we will end up with packages compiled for whatever OS the user is running instead of debian
COPY . .

# 9090 for relay server; 5001 for ipfs rpc api
EXPOSE 9090 5001 4001

CMD bash -c "ipfs init && ipfs daemon && ipfs swarm peers & sleep 3 && chmod +x publish-relay.sh && node relay.js"