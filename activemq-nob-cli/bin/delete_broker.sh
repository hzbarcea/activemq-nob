#!/bin/sh
######################################################################################################################
##
## PROGRAM: delete_broker.sh
##
######################################################################################################################

JSON_PRETTY_PRINT="json_pp"

# DEBUG="-D headers.001 --trace trace.001"
DEBUG=""

UUID="${1}"
URL="http://localhost:9000/nob/broker/${UUID}"

if [ -z "$UUID" ]
then
	echo "Usage $0 <broker-id>" >&2
	exit 1
fi

RESULT="$(curl -X DELETE -s -S $DEBUG "$URL")"
if PP="$(echo "$RESULT" | "$JSON_PRETTY_PRINT" 2>/dev/null)"
then
	echo "$PP"
else
	echo "$RESULT"
fi

echo
echo

