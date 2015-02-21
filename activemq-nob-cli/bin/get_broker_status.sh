#!/bin/sh
######################################################################################################################
##
## PROGRAM: get_broker_status.sh
##
######################################################################################################################

# DEBUG="-D headers.001 --trace trace.001"
DEBUG=""

UUID="${1}"
URL="http://localhost:9000/nob/broker/${UUID}/status"

if [ -z "$UUID" ]
then
	echo "Usage: $0 <broker-id>" >&2
	exit 1
fi

curl --get -s -S $DEBUG "$URL"
echo

