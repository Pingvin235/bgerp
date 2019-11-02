#!/bin/sh

cd ${0%${0##*/}}.

./erp_stop.sh && ./erp_start.sh