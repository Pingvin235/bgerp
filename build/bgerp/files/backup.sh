#!/bin/sh

cd ${0%${0##*/}}.

DIR="backup"

DATE=/bin/date
ZIP=/usr/bin/zip
MYSQLDUMP=/usr/bin/mysqldump

# list of included folders and files
BACKUP_STRING="docpattern lib plugin report webapps"

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
    $MYSQLDUMP -h $HOST -P $PORT -u $USER -p$PWD -A -R > dump.sql
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