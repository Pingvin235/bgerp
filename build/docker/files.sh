#!/bin/sh

cd ${0%${0##*/}}.

rm -rf files && mkdir files

cp -v ../../target/distributions/bgerp_*.zip files
wget https://demo.bgerp.org/bgerp.sql -O files/bgerp.sql
wget https://demo.bgerp.org/filestorage.zip -O files/filestorage.zip

rename -v 's/(\w+)_[\d\._]+(\.zip)$/$1$2/' files/*
