#!/bin/bash

set -e

RELAY_DIR="./tp-relay-ipfs"
RELAY_FILE="relay-addr.txt"
META_FILE="meta.json"
KEY_NAME="tp-relay-key"

# Ensure the directory to publish exists
mkdir -p "$RELAY_DIR"

# Copy the latest relay address into the directory
cp "$RELAY_FILE" "$RELAY_DIR/relay-addr.txt"

# Add timestamped metadata
cat <<EOF > "$RELAY_DIR/$META_FILE"
{
  "updated": "$(date -Iseconds)"
}
EOF

# Add and pin the directory to ipfs
CID=$(ipfs add -r -Q "$RELAY_DIR")
ipfs pin add "$CID"

echo "✅ Published $RELAY_DIR to IPFS with CID: $CID"

# Publish to IPNS using named key
IPNS_RESULT=$(ipfs name publish --key="$KEY_NAME" "/ipfs/$CID")
echo "🌍 IPNS Published:"
echo "$IPNS_RESULT"