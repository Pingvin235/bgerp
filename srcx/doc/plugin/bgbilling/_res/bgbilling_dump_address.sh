#!/bin/sh

BASE="bgbilling"
HOST="127.0.0.1"
LOGIN="root"
PSWD=""

SRC_CHARSET="cp1251"
TARGET_CHARSET="utf8"

BASE_TEMP="_bg_address_tables"
DUMP_FILE="address_data.sql"

COMMAND_SQL="mysql -u$LOGIN -p$PSWD" 
COMMAND_DUMP="mysqldump -u$LOGIN -p$PSWD --default-character-set=$SRC_CHARSET --skip-set-charset" 

echo "Copy address data to tables in temporary database $BASE_TEMP."

echo "\
DROP DATABASE IF EXISTS $BASE_TEMP;\
CREATE DATABASE $BASE_TEMP DEFAULT CHARSET $SRC_CHARSET;\
USE $BASE_TEMP;\
\
CREATE TABLE  address_area (\
    id int NOT NULL,\
    city_id int NOT NULL DEFAULT '-1',\
    title varchar(150) NOT NULL,\
    last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);\
\
CREATE TABLE  address_city (\
    id int(11) NOT NULL,\
    country_id int(11) NOT NULL DEFAULT '-1',\
     title varchar(150) NOT NULL,\
    last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);\
\
CREATE TABLE  address_country (\
    id int(11) NOT NULL,\
    title varchar(255) NOT NULL,\
    last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);\
\
CREATE TABLE  address_house (\
    id int(11) NOT NULL,\
    area_id int(11) NOT NULL,\
    quarter_id int(11) NOT NULL,\
    street_id int(11) NOT NULL,\
    house int(11) NOT NULL,\
    frac varchar(30) NOT NULL,\
    post_index varchar(10) NOT NULL,\
    comment varchar(100) NOT NULL,\
    last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);\
\
CREATE TABLE  address_quarter (\
    id int(10) NOT NULL,\
    city_id int(11) NOT NULL DEFAULT '-1',\
    title varchar(150) NOT NULL,\
    last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);\
\
CREATE TABLE  address_street (\
    id int(10) NOT NULL,\
    city_id int(11) NOT NULL DEFAULT '-1',\
    title varchar(150) NOT NULL DEFAULT '',\
    last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);\
\
INSERT INTO $BASE_TEMP.address_area(id, city_id, title) SELECT id, cityid, title FROM $BASE.address_area;\
INSERT INTO $BASE_TEMP.address_city(id, country_id, title) SELECT id, country_id, title FROM $BASE.address_city;\
INSERT INTO $BASE_TEMP.address_country(id, title) SELECT id, title FROM $BASE.address_country;\
INSERT INTO $BASE_TEMP.address_house(id, area_id, quarter_id, street_id, house, frac, post_index, comment) SELECT id, areaid, quarterid, streetid, house, frac, box_index, comment FROM $BASE.address_house;\
INSERT INTO $BASE_TEMP.address_quarter(id, city_id, title) SELECT id, cityid, title FROM $BASE.address_quarter;\
INSERT INTO $BASE_TEMP.address_street(id, city_id, title) SELECT id, cityid, title FROM $BASE.address_street;\
" | $COMMAND_SQL

echo "Dumping temporary database to $DUMP_FILE."

echo "\
DELETE FROM address_area;\
DELETE FROM address_city;\
DELETE FROM address_country;\
DELETE FROM address_house;\
DELETE FROM address_quarter;\
DELETE FROM address_street;\
" > ./$DUMP_FILE

$COMMAND_DUMP -t $BASE_TEMP >> ./$DUMP_FILE

echo "DROP DATABASE $BASE_TEMP" | $COMMAND_SQL

echo "Change charset."

iconv -c -f $SRC_CHARSET -t $TARGET_CHARSET $DUMP_FILE > ./"$DUMP_FILE.CONV"
mv $DUMP_FILE.CONV $DUMP_FILE