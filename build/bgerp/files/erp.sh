#!/bin/sh

cd ${0%${0##*/}}.

. ./setenv.sh

BGERP_DIR=.
CLASSPATH=${BGERP_DIR}:${BGERP_DIR}/lib/ext/*:${BGERP_DIR}/lib/app/*:${BGERP_DIR}/lib/custom/*

if [ ! -d "${BGERP_DIR}/.run" ] ; then
    mkdir ${BGERP_DIR}/.run
fi

PARAMS="-Djava.security.egd=file:/dev/./urandom -Dbgerp.setup.data=bgerp -Djava.net.preferIPv4Stack=true -Dnetworkaddress.cache.ttl=3600 -Djava.awt.headless=true"

if [ "$1" = "start" ]; then
	#starting
    nohup  ${JAVA_HOME}/bin/java ${PARAMS} -Xmx600m -cp ${CLASSPATH} ru.bgerp.Server $1 $2 $3 > ./log/bgerp.out 2>&1 & echo $! > .run/bgerp.pid &
else
	if [ "$1" = "debug" ]; then
		#starting in debug mode
	    nohup  ${JAVA_HOME}/bin/java ${PARAMS} -enableassertions -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=5599,server=y,suspend=n -Xmx600m -cp ${CLASSPATH} ru.bgerp.Server start $2 $3 > ./log/bgerp.out 2>&1 & echo $! > .run/bgerp.pid &
	else
		#execute command - quotes for transforming multiple for one params
		${JAVA_HOME}/bin/java ${PARAMS} -cp ${CLASSPATH} ru.bgerp.Server "$1 $2 $3"
	fi
fi