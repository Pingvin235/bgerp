#!/bin/sh

cd ${0%${0##*/}}.

. ./setenv.sh

PID=`cat ./.run/bgcrm.pid`

if [ -z "$1" ]; then
    echo "Dump name is not defined"
    exit 1
fi

./crm.sh gc

sleep 5

$JAVA_HOME/bin/jmap -dump:format=b,file=$1 $PID