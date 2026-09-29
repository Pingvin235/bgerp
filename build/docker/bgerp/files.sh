#!/bin/sh

cd ${0%${0##*/}}.

rm -rf files && mkdir files

cp -v ../../../target/distributions/bgerp_*.zip files

rename -v 's/(\w+)_[\d\._]+(\.zip)$/$1$2/' files/*
