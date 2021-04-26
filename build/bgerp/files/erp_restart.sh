#!/bin/sh

cd ${0%${0##*/}}.

if [ "$1" = "force" ]; then
    ./erp_kill.sh && ./erp_start.sh
else
    ./erp_stop.sh && ./erp_start.sh
fi
