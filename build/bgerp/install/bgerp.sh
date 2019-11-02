#!/bin/sh

echo "Checking prerequisite utilities (unzip)"
[ -n "`which unzip`" ]

ERP_ZIP=$1

unzip -o $ERP_ZIP 'BGERP/*' -d /opt
unzip -o $ERP_ZIP 'db.sql*' -d /opt/BGERP

