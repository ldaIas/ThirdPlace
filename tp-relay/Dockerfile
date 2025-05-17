FROM node:20

WORKDIR /app

# Only copy package files first (for caching)
COPY package.json package-lock.json ./

# Install the dependencies
RUN npm install

# Copy the rest of the code (except whats in .dockerignore)
# If we do this without installing first, we will end up with packages compiled for whatever OS the user is running instead of debian
COPY . .

EXPOSE 9090

CMD ["node", "relay.js"]
