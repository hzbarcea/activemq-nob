#!/bin/sh
######################################################################################################################
##
## PROGRAM: update_broker_xbean.sh
##
######################################################################################################################

JSON_PRETTY_PRINT="json_pp"

# DEBUG="-D headers.001 --trace trace.001"
DEBUG=""

UUID="${1}"

if [ -z "$UUID" ]
then
	echo "Usage: $0 <broker-id>" >&2
	exit 1
fi

URL="http://localhost:9000/nob/broker/${UUID}/xbean"

NEW_XBEAN_CONTENT="!!! PUT NEW CONTENT HERE !!!"$'\n'

RESULT="$(curl -X PUT -d "$NEW_XBEAN_CONTENT" -H "Content-Type: application/xml" -s -S $DEBUG "$URL")"
if PP="$(echo "$RESULT" | "$JSON_PRETTY_PRINT" 2>/dev/null)"
then
	echo "$PP"
else
	echo "$RESULT"
fi

echo
echo

