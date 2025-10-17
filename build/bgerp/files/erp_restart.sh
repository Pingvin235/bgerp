#!/bin/bash

cd ${0%${0##*/}}.

NAME=$(basename "$0")
if [[ ! -O "$NAME" ]]; then
    echo "Only the file's owner is allowed to restart BGERP!"
    exit 1
fi

if [ "$1" = "force" ]; then
    ./erp_kill.sh && ./erp_start.sh
else
    ./erp_stop.sh && ./erp_start.sh
fi
