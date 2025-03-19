#!/bin/sh
#
# Script takes auth data and DB name from bgerp.properties file and runs mysql client with them.
#
# Running modes:
# 1) Without arguments, interactive mode:
# ./mysql.sh
#
# 2) With quoted query as a parameter:
# ./mysql.sh "SELECT * FROM user LIMIT 1;"
# or
# ./mysql.sh "SELECT * FROM user LIMIT 1; SELECT * FROM process LIMIT 1;"
#
# 3) With SQL script file name as a parameter:
# ./mysql.sh < script.sql

cd ${0%${0##*/}}.

FILE=bgerp.properties
MYSQL=/usr/bin/mariadb
if [ ! -f $MYSQL ]; then
    MYSQL=/usr/bin/mysql
fi

PWD=`grep db.pswd $FILE | cut -d'=' -f2`
USER=`grep db.user $FILE | cut -d'=' -f2`
HOST_PORT=`grep db.url $FILE | cut -d'/' -f3`
HOST=`echo $HOST_PORT | cut -d':' -f1`
PORT=`echo $HOST_PORT | cut -d':' -f2`
if [ "$PORT" = "$HOST" ] ; then
    PORT=3306
fi
DB=`grep db.url $FILE | cut -d'?' -f1 | cut -d'/' -f4`

CMD="$MYSQL -h $HOST -P $PORT -u$USER -p$PWD $DB"

if test -n "$1"; then
    echo "$1" | $CMD
elif test ! -t 0; then
    cat - | $CMD
else
    $CMD
fi
