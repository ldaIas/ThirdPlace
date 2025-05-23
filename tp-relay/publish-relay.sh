#!/bin/bash
# NOT A DEV SCRIPT, DON'T MOVE
# This script builds up the /tp-relay-ipfs/ directory, which is where the info is stored for the relay server information

set -e

# Resolve base directory (assume this is also in tp-relay/dev_scripts)
TP_ROOT="$(pwd)"
IPFS_DIR="$TP_ROOT/${IPFS_PATH_NAME:-tp-ipfs}"
export IPFS_PATH="$IPFS_DIR"

LOCALCONF="$IPFS_DIR/localconf.env"
if [ ! -f "$LOCALCONF" ]; then
  echo "❌ Missing localconf.env in $IPFS_DIR"
  echo "Run init_ipfs.sh first"
  exit 1
fi


if [ ! -f "$LOCALCONF" ]; then
  echo "❌ Missing local config at $LOCALCONF"
  echo "Please create it with: IPFS_IDENTITY_KEY_NAME=your-key-name"
  exit 1
fi

source "$LOCALCONF"

# Default fallback values
KEY_NAME="${IPFS_IDENTITY_KEY_NAME:-thirdplace-relay-key}"


# File structure
RELAY_DIR="$TP_ROOT/tp-relay"
IPFS_SUBDIR="$RELAY_DIR/ipfs"
RELAY_FILE="$RELAY_DIR/relay-addr.txt"
META_FILE="meta.json"

mkdir -p "$IPFS_SUBDIR"

# Copy latest relay address
cp "$RELAY_FILE" "$IPFS_SUBDIR/relay-addr.txt"

# Add timestamp metadata
cat <<EOF > "$IPFS_SUBDIR/$META_FILE"
{
  "updated": "$(date -Iseconds)"
}
EOF

# Add and pin the folder
CID=$(ipfs add -r -Q "$IPFS_SUBDIR")
ipfs pin add "$CID"

echo "✅ Published $IPFS_SUBDIR to IPFS with CID: $CID"

# Publish to IPNS via configured key
if ! ipfs key list | grep -q "$KEY_NAME"; then
  echo "❌ Key '$KEY_NAME' not found in IPFS repo at $IPFS_PATH"
  exit 1
fi

IPNS_RESULT=$(ipfs name publish --key="$KEY_NAME" "/ipfs/$CID")
echo "🌍 Published to IPNS:"
echo "$IPNS_RESULT"
