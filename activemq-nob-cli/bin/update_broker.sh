#!/bin/sh
######################################################################################################################
##
## PROGRAM: update_broker.sh
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


URL="http://localhost:9000/nob/broker/${UUID}"
NOW="$(date '+%Y-%m-%dT%H:%M:%S%z')"

BROKER_INFO="\
{ \
   \"broker\" : [ { \"status\" : \"UPDATED\", \
      \"name\" : \"${UUID}\", \
      \"id\" : \"${UUID}\", \
      \"lastModifiedXbean\" : \"${NOW}\", \
   } \
   ] \
} \
"

RESULT="$(curl -X PUT -H "Content-Type: application/json" -d "$BROKER_INFO" -s -S $DEBUG "$URL")"
if PP="$(echo "$RESULT" | "$JSON_PRETTY_PRINT" 2>/dev/null)"
then
	echo "$PP"
else
	echo "$RESULT"
fi

echo
echo

