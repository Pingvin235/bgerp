#!/bin/sh

VERSION=`cat common/VERSION`

SSH_ACCOUNT="www@bgerp.ru"
DEST_DIR="www.bgerp.ru/doc/$VERSION"

TMP_DIR="/tmp"

JAVA_DOC_DIR="javadoc"
JAVA_DOC_ZIP="bgcrm_${VERSION}_javadoc.zip"

JAVA_DOC_DIR_LOCAL="$TMP_DIR/$JAVA_DOC_DIR"
JAVA_DOC_ZIP_LOCAL="$TMP_DIR/$JAVA_DOC_ZIP"

rm -rf $JAVA_DOC_DIR_LOCAL
mkdir $JAVA_DOC_DIR_LOCAL
rm -f $JAVA_DOC_ZIP_LOCAL

cd ./apidoc
ant
cp -R ./$JAVA_DOC_DIR/* $JAVA_DOC_DIR_LOCAL

cd $JAVA_DOC_DIR_LOCAL
zip -R $JAVA_DOC_ZIP_LOCAL './*'

#copy on server
scp -r $JAVA_DOC_ZIP_LOCAL $SSH_ACCOUNT:$DEST_DIR
ssh $SSH_ACCOUNT "rm -rf $DEST_DIR/$JAVA_DOC_DIR/*"
ssh $SSH_ACCOUNT "unzip $DEST_DIR/$JAVA_DOC_ZIP -d $DEST_DIR/$JAVA_DOC_DIR"

#ssh $SSH_ACCOUNT "chown -R www:users $DEST_DIR"
#ssh $SSH_ACCOUNT "chmod -R 755 $DEST_DIR/$JAVA_DOC_DIR"