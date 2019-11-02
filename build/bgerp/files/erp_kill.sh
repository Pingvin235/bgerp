#!/bin/sh

cd ${0%${0##*/}}.

PID=`cat .run/bgerp.pid`

echo "Killing: $PID"

kill -9 $PID
