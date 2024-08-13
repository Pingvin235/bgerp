#!/bin/bash

cd ${0%${0##*/}}.

. ./setenv.sh

. ./jcheck.sh

TEE=/usr/bin/tee
DATE=/bin/date

time=`${DATE} +%Y-%m-%d_%H:%M:%S`

BGERP_DIR=.
CLASSPATH=${BGERP_DIR}:${BGERP_DIR}/lib/ext/*:${BGERP_DIR}/lib/app/*

PARAMS="-Dbgerp.setup.data=bgerp -Dlog4j.configuration=file:./log4j_installer.properties"

${JAVA_HOME}/bin/java ${PARAMS} -cp ${CLASSPATH} org.bgerp.Installer $1 $2 $3 2>&1 | ${TEE} ./log/update_${time}.log

# delete more that 10 oldest log/update_ files
cd log && ls -1t . | grep -E "update.+log" | tail -n +11 | xargs rm -f && cd ..

# add execution rights on new scripts
chmod +x ./*.sh

# delete log_update files, old storing place
find . -type f -name 'log_update*' -exec rm {} \;

# delete update modules, old storing place
ls -1t . | grep -E "update.+zip" | xargs rm -f
