#!/bin/sh

cd ${0%${0##*/}}.

. ./setenv.sh

TEE=/usr/bin/tee
DATE=/bin/date

time=`${DATE} +%Y-%m-%d_%H:%M:%S`

BGERP_DIR=.
CLASSPATH=${BGERP_DIR}:${BGERP_DIR}/lib/ext/*:${BGERP_DIR}/lib/app/*

PARAMS="-Dbgerp.setup.data=bgerp -Dbgerpnet.preferIPv4Stack=true -Dnetworkaddress.cache.ttl=3600 -Djava.awt.headless=true"

${JAVA_HOME}/bin/java ${PARAMS} -cp ${CLASSPATH} ru.bgcrm.util.distr.Installer $1 $2 $3 2>&1 | ${TEE} ./log/update_${time}.log

# delete more that 10 oldest log/update_ files
cd log && ls -1t . | grep -E "update.+log" | tail -n +11 | xargs rm -f && cd ..

# delete log_update older than 30 days, old format
find . -type f -name 'log_update*' -mtime +30 -exec rm {} \;

# delete old update packages
ls -1t . | grep -E "update.+zip" | tail -n +11 | xargs rm -f

# clean up compiled JSP
rm -rf ./work/*

# add execution rights on new scripts
chmod +x ./*.sh
