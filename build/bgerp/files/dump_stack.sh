#!/bin/bash

cd ${0%${0##*/}}.

. ./setenv.sh

. ./jcheck.sh

PID=`cat ./.run/bgerp.pid`

if [ -z "$1" ]; then
    echo "Dump name is not defined"
    exit 1
fi

$JAVA_HOME/bin/jstack  $PID > $1