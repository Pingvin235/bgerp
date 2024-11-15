#!/bin/bash

cd ${0%${0##*/}}.

# apps used
DATE=/bin/date
ZIP=/usr/bin/zip
UNZIP=/usr/bin/unzip
MYSQLDUMP=/usr/bin/mysqldump
MYSQL=/usr/bin/mysql
PV=/usr/bin/pv

# parse database access from bgerp.properties
FILE=bgerp.properties

while IFS="=" read -r key value; do
    case "$key" in
        "db.url")
            if [[ "${value}" =~ ([[:alpha:][:digit:]_\.\-]+):*([[:digit:]]*)/([[:alpha:][:digit:]_\.\-]+) ]]; then
                HOST_SQL=${BASH_REMATCH[1]}
                PORT_SQL=${BASH_REMATCH[2]}
                DBASE_NAME=${BASH_REMATCH[3]}
            fi
        ;;
        "db.user")
            USER_SQL="$value"
        ;;
        "db.pswd")
            PASSWORD_SQL="$value"
        ;;
    esac
done < "$FILE"

if [ -n "$PORT_SQL" ]; then
    PORT_SQL=" -P $PORT_SQL"
fi

# constants
BACKUP_DIR=backup
BACKUP_TMP=backup_tmp
DUMP=dump.sql
LIB=lib
WEBAPPS=webapps

# exit from script on any error
set -e

function create () {
    if [ ! -d ${BACKUP_DIR} ] ; then
        mkdir ${BACKUP_DIR}
    fi

    rm -rf ${BACKUP_TMP}
    mkdir ${BACKUP_TMP}

    cd ${BACKUP_TMP}

    cp -r ../${LIB} ./
    cp -r ../${WEBAPPS} ./

    CUR_DATE=`$DATE +%Y-%m-%d_%H_%M`
    CUR_BACKUP_NAME=${CUR_DATE}.zip

    echo "Backup name: $CUR_BACKUP_NAME"

    ${ZIP} -r ../${BACKUP_DIR}/${CUR_BACKUP_NAME} ${LIB} ${WEBAPPS}
    cd ..

    rm -rf ${BACKUP_TMP}
}

function create_db () {
    if [ ! -d ${BACKUP_DIR} ] ; then
        mkdir ${BACKUP_DIR}
    fi

    rm -rf ${BACKUP_TMP}
    mkdir ${BACKUP_TMP}

    cd ${BACKUP_TMP}
    ${MYSQLDUMP} --single-transaction --no-tablespaces --routines --host=${HOST_SQL} --user=${USER_SQL} --password=${PASSWORD_SQL} ${PORT_SQL} ${DBASE_NAME} > ${DUMP}

    cp -r ../${LIB} ./
    cp -r ../${WEBAPPS} ./

    CUR_DATE=`$DATE +%Y-%m-%d_%H_%M`
    CUR_BACKUP_NAME=${CUR_DATE}.db.zip

    echo "Backup name: $CUR_BACKUP_NAME"

    ${ZIP} -rq ../${BACKUP_DIR}/${CUR_BACKUP_NAME} ${DUMP} ${LIB} ${WEBAPPS}
    cd ..

    rm -rf ${BACKUP_TMP}
}

function restore () {
    rm -rf ${BACKUP_TMP}

    mkdir ${BACKUP_TMP}
    cd ${BACKUP_TMP}

    ${UNZIP} ../${BACKUP_DIR}/$1

    rm -rf ../${LIB}
    cp -r ${LIB} ../${LIB}
    rm -rf ../${WEBAPPS}
    cp -r ${WEBAPPS} ../${WEBAPPS}

    ${MYSQL} -B --host=${HOST_SQL} --user=${USER_SQL} --password=${PASSWORD_SQL} ${PORT_SQL} -e "CREATE DATABASE IF NOT EXISTS ${DBASE_NAME}"
    ${MYSQL} -B --host=${HOST_SQL} --user=${USER_SQL} --password=${PASSWORD_SQL} ${PORT_SQL} ${DBASE_NAME} < ${DUMP}

    cd ..
    rm -rf ${BACKUP_TMP}
}

function help {
    echo "USAGE: ./backup.sh [CMD]"
    echo "Where [CMD] in:"
    echo " * create - to create backup in 'backup' directory"
    echo " * create_db - to create backup and MySQL dump in 'backup' directory"
    echo " * restore [FILE] - to restore state to [FILE]'s backup state"
    exit 1
}

if [ -z $1 ] ; then
    help
fi

if [ "$1" = "create" ] ; then
    create
elif [ "$1" = "create_db" ] ; then
    create_db
else
    if [ "$1" = "restore" ] ; then
        if [ -z $2 ] ; then
            echo "no backup file given"
            echo
            help
        else
            restore $2
        fi
    else
        echo "unknown command '$1'"
        echo
        help
    fi
fi

exit 0
