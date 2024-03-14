#!/bin/bash

# generated each time, but used only on init first time
export MYSQL_ROOT_PASSWORD="$(pwgen -1 32)"

source docker-bgerp-mysql.sh

# $MYSQL_ROOT_PASSWORD will be used there in case of missing DB
# kick off the upstream command
/usr/local/bin/docker-entrypoint.sh mysqld &

source docker-bgerp-data.sh

echo "BGERP Dev DB is running"

exec tail -f /dev/null
