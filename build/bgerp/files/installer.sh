#!/bin/bash

cd ${0%${0##*/}}.

. ./setenv.sh

. ./jcheck.sh

DATE=/bin/date
TEE=/usr/bin/tee

TIME=`${DATE} +%Y-%m-%d_%H:%M:%S`

PARAMS="-Dbgerp.setup.data=bgerp -Dlog4j.configuration=file:./log4j_installer.properties"
CLASSPATH=.:./lib/ext/*:./lib/app/*

$JAVA_HOME/bin/java $PARAMS -cp $CLASSPATH org.bgerp.Installer $1 $2 $3 2>&1 | $TEE ./log/update_${TIME}.log

EXIT_CODE=${PIPESTATUS[0]}

# delete more that 10 oldest log/update_ files
cd log && ls -1t . | grep -E "update.+log" | tail -n +11 | xargs rm -f && cd ..

# add execution rights on new scripts
chmod +x ./*.sh

exit $EXIT_CODE