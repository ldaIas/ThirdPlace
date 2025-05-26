FROM node:20

# Install IPFS
RUN curl -O https://dist.ipfs.tech/kubo/v0.25.0/kubo_v0.25.0_linux-amd64.tar.gz && \
    tar -xvzf kubo_v0.25.0_linux-amd64.tar.gz && \
    cd kubo && \
    bash install.sh && \
    ipfs --version

ENV IPFS_PATH=/ipfs

WORKDIR /app

# Only copy package files first (for caching)
COPY package.json package-lock.json ./

# Install the dependencies
RUN npm install

# Copy the rest of the code (except whats in .dockerignore)
# If we do this without installing first, we will end up with packages compiled for whatever OS the user is running instead of debian
COPY . .

EXPOSE 9090

CMD bash -c "ipfs daemon & sleep 3 && node relay.js"