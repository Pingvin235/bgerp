#!/bin/sh

cd ${0%${0##*/}}.

DIR="backup"

DATE=/bin/date
ZIP=/usr/bin/zip
MYSQLDUMP=/usr/bin/mysqldump

# list of included folders and files
BACKUP_STRING="lib webapps"

mkdir -p $DIR
time=`$DATE +%Y-%m-%d_%H_%M`
file="$DIR/$time"

backup_db() {
    echo "Making dump DB MySQL!"
    FILE=bgerp.properties
    PWD=`grep db.pswd $FILE | cut -d'=' -f2`
    USER=`grep db.user $FILE | cut -d'=' -f2`
    FULLLINK=`grep db.url $FILE | cut -d'/' -f3`
    HOST=`echo $FULLLINK | cut -d':' -f1`
    PORT=`echo $FULLLINK | cut -d':' -f2`
    if [ "$PORT" = "$HOST" ] ; then
      PORT=3306
    fi
    DB=`grep db.url $FILE | cut -d'?' -f1 | cut -d'/' -f4`
    CMD="$MYSQLDUMP --no-tablespaces -h $HOST -P $PORT -u $USER $DB"
    echo "dump command: $CMD"
    CMD="$CMD -p$PWD"
    $CMD > dump.sql
    BACKUP_STRING="$BACKUP_STRING dump.sql"
    file="$file.db"
}

while [ -n "$1" ] ; do
    if [ "$1" = "db" ] ; then
        backup_db
    fi
    shift
done

file="$file.zip"
echo "Making backup to $file"

$ZIP -r $file $BACKUP_STRING

echo "Backup $BACKUP_STRING is done to $file"