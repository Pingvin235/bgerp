#!/bin/sh

cd ${0%${0##*/}}.

. ./setenv.sh

TEE=/usr/bin/tee
DATE=/bin/date

time=`${DATE} +%Y-%m-%d_%H:%M:%S` 

BGERP_DIR=.
CLASSPATH=${BGERP_DIR}:${BGERP_DIR}/lib/ext/*:${BGERP_DIR}/lib/app/*

PARAMS="-Dbgerp.setup.data=bgerp -Dbgerpnet.preferIPv4Stack=true -Dnetworkaddress.cache.ttl=3600 -Djava.awt.headless=true"

${JAVA_HOME}/bin/java ${PARAMS} -cp ${CLASSPATH} ru.bgcrm.util.distr.Installer $1 $2 $3 | ${TEE} ./log_update_${time}

rm -rf ./work/*
