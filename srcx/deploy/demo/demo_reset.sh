#!/bin/bash

DUMP_FILE="/home/demo/bgerp.sql"
ERP_DIR="/home/demo/bgerp"

echo "Applying dump"

mysql --default-character-set=utf8 -ubgerp_demo -pxxxxx bgerp_demo < $DUMP_FILE

echo "Extracting filestorage.zip"

unzip filestorage.zip -d $ERP_DIR/filestorage

echo "Downloading demo license"

wget https://bgerp.org/download/lic.data -O "$ERP_DIR/lic.data"

echo "Installing 00000"

cd $ERP_DIR && ./installer.sh installc 00000 && ./erp_restart.sh
