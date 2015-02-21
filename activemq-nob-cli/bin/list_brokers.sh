#!/bin/sh
######################################################################################################################
##
## PROGRAM: list_brokers
##
######################################################################################################################

JSON_PRETTY_PRINT="json_pp"

# DEBUG="-D headers.001 --trace trace.001"
DEBUG=""
URL="http://localhost:9000/nob/brokers"

RESULT="$(curl --get -s -S $DEBUG "$URL")"
if PP="$(echo "$RESULT" | "$JSON_PRETTY_PRINT" 2>/dev/null)"
then
	echo "$PP"
else
	echo "$RESULT"
fi

echo
echo
