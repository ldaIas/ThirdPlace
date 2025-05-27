#!/bin/bash
# NOT A DEV SCRIPT, DON'T MOVE
# This script builds up the /tp-relay-ipfs/ directory, which is where the info is stored for the relay server information

set -e

# Resolve base directory
TP_ROOT="$(pwd)"
IPFS_DIR="$TP_ROOT/${IPFS_PATH_NAME:-tp-ipfs}"
export IPFS_PATH="$IPFS_DIR"

echo "Exported IPFS path: $IPFS_PATH"

# init if needed
if [ ! -d "$IPFS_PATH/plugins" ]; then
  mkdir -p "$IPFS_PATH/plugins"
fi

LOCALCONF="localconf.env"

if [ ! -f "$LOCALCONF" ]; then
  echo "❌ Missing local config at $LOCALCONF"
  echo "Please create it with the key: IPFS_IDENTITY_KEY_NAME=your-key-name"
  echo "This can be done with the dev script init_ipfs.sh"
  exit 1
fi

source "$LOCALCONF"

echo "✅ Loaded local config from $LOCALCONF:"
head -n 10 "$LOCALCONF"
echo "%n"

# Default fallback values
KEY_NAME="${IPFS_IDENTITY_KEY_NAME:-thirdplace-relay-key}"


# File structure
RELAY_DIR="$TP_ROOT/tp-relay-ipfs"
META_FILE="meta.json"

mkdir -p "$RELAY_DIR"

# Add timestamp metadata
cat <<EOF > "$RELAY_DIR/$META_FILE"
{
  "updated": "$(date -Iseconds)"
}
EOF

# Add and pin the folder
CID=$(ipfs add -r -Q "$RELAY_DIR")
ipfs pin add "$CID"

echo "✅ Published $RELAY_DIR to IPFS with CID: $CID"

# Publish to IPNS via configured key
if ! ipfs key list | grep -q "$KEY_NAME"; then
  echo "❌ Key '$KEY_NAME' not found in IPFS repo at $IPFS_PATH"
  exit 1
fi

IPNS_RESULT=$(ipfs name publish --key="$KEY_NAME" "/ipfs/$CID")
echo "🌍 Published to IPNS:"
echo "$IPNS_RESULT"