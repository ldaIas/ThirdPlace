#!/bin/bash
# This script initializes the /tp-relay/tp-ipfs/ directory which is used to publish relay server address to IPFS

echo "===⚡init-ipfs.sh⚡==="

set -e

# Get directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TP_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
IPFS_DIR="$TP_ROOT/tp-ipfs"
export IPFS_PATH="$IPFS_DIR"

# Config from user profile
LOCALCONF="$SCRIPT_DIR/.localconf"

if [ ! -f "$LOCALCONF" ]; then
  echo "❌ Missing local config at $LOCALCONF"
  echo "Please create it with: IPFS_IDENTITY_KEY_NAME=your-key-name"
  exit 1
fi

# Load config
source "$LOCALCONF"

# Fallback defaults
KEY_NAME="${IPFS_IDENTITY_KEY_NAME:-thirdplace-relay-key}"
IPFS_DIR="$TP_ROOT/${IPFS_PATH_NAME:-tp-ipfs}"
export IPFS_PATH="$IPFS_DIR"

echo "🔧 Initializing IPFS repo at $IPFS_PATH using key '$KEY_NAME'"

mkdir -p "$IPFS_PATH"

if [ ! -f "$IPFS_PATH/config" ]; then
  ipfs init
  echo "✅ IPFS initialized"

  if ! ipfs key list | grep -q "$KEY_NAME"; then
    ipfs key gen --type=rsa --size=2048 "$KEY_NAME"
    echo "🔑 Generated key '$KEY_NAME'"
  fi

    cat <<EOF > "$IPFS_PATH/localconf.env"
IPFS_IDENTITY_KEY_NAME=$KEY_NAME
IPFS_PATH_NAME=$IPFS_DIR
EOF
  echo "📝 Wrote localconf.env to $IPFS_PATH"
else
  echo "ℹ️ IPFS already initialized at $IPFS_PATH"
fi
