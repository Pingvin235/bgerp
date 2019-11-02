#!/bin/sh

cd ${0%${0##*/}}.

. ./setenv.sh

PID=`cat ./.run/bgcrm.pid`

if [ -z "$1" ]; then
    echo "Dump name is not defined"
    exit 1
fi

$JAVA_HOME/bin/jstack  $PID > $1 