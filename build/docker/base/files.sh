#!/bin/sh

cd ${0%${0##*/}}.

rm -rf files && mkdir files

cp -v ./../../bgerp/files/bgerp.properties files
cp -v ./../../bgerp/db_create.sql files
