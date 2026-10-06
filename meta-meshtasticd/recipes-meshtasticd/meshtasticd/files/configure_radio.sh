#!/bin/sh
set -x

REGION="${1:-US}"
HOST="${2:-localhost}"
CHANNEL="${3:-mesh1}"
IDX=1 # new channel index

echo "Creating a new channel: $CHANNEL"

meshtastic \
    --host "$HOST" \
    --set lora.region "$REGION" \
    --set position.gps_mode ENABLED \
    --ch-add "$CHANNEL" \
    --ch-index "$IDX" \
    --ch-set psk random \
    --ch-set uplink_enabled true \
    --ch-set downlink_enabled true

echo "Region set to $REGION and GPS enabled."
