#!/bin/bash

cd ${0%${0##*/}}.

. ./setenv.sh

. ./jcheck.sh

DATE=/bin/date

time=`${DATE} +%Y-%m-%d_%H:%M:%S`

BGERP_DIR=.
CLASSPATH=${BGERP_DIR}:${BGERP_DIR}/lib/ext/*:${BGERP_DIR}/lib/app/*:${BGERP_DIR}/lib/custom/*

if [ ! -d "${BGERP_DIR}/.run" ] ; then
    mkdir ${BGERP_DIR}/.run
fi

PARAMS="-Djava.security.egd=file:/dev/./urandom -Dbgerp.setup.data=bgerp -Djava.net.preferIPv4Stack=true -Dnetworkaddress.cache.ttl=3600 -Djava.awt.headless=true"
MEMORY="-Xmx600m"
DEBUG="-enableassertions -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=5599,server=y,suspend=n"

if [ "$1" = "start" ]; then
    #starting
    nohup ${JAVA_HOME}/bin/java ${PARAMS} ${MEMORY} -cp ${CLASSPATH} org.bgerp.Server $1 $2 $3 > ./log/bgerp_${time}.out 2>&1 & echo $! > .run/bgerp.pid &
    # delete more that 10 oldest log/bgerp.out files
    ls -1t ./log/bgerp*.out | tail -n +11 | xargs rm -f
elif [ "$1" = "debug" ]; then
    #starting in debug mode
    nohup ${JAVA_HOME}/bin/java ${PARAMS} ${MEMORY} ${DEBUG} -cp ${CLASSPATH} org.bgerp.Server start $2 $3 > ./log/bgerp_${time}.out 2>&1 & echo $! > .run/bgerp.pid &
elif [ "$1" = "docker" ]; then
    ${JAVA_HOME}/bin/java ${PARAMS} ${MEMORY} -cp ${CLASSPATH} org.bgerp.Server start $2 $3
else
    #execute command - quotes for transforming multiple for one params
    ${JAVA_HOME}/bin/java ${PARAMS} -cp ${CLASSPATH} org.bgerp.Server "$1 $2 $3"
fi
