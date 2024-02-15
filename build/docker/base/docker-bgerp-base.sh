#!/bin/bash

# generated each time, but used only on init first time
export MYSQL_ROOT_PASSWORD="$(pwgen -1 32)"

echo "Running MySQL"

docker_wait_mysql_up () {
    for i in {300..0}; do
        if mysqladmin ping -h localhost --silent; then
            break
        fi
        sleep 2
    done
    if [ "$i" = 0 ]; then
        echo `date`" Unable to start MySQL server."
        exit 1
    else
        echo `date`" MySQL started, $i."
    fi
}

docker_wait_mysql_down () {
    for i in {300..0}; do
        if ! mysqladmin ping -h localhost --silent; then
            break
        fi
        sleep 1
    done
    if [ "$i" = 0 ]; then
        echo `date`" Unable to stop MySQL server."
        exit 1
    else
        echo `date`" MySQL stopped, $i."
    fi
}

# $MYSQL_ROOT_PASSWORD will be used there in case of missing DB
# kick off the upstream command
/usr/local/bin/docker-entrypoint.sh mysqld &

if [ ! -d "/var/lib/mysql/mysql" ]; then
    echo "MySQL data directory init"

    # temporary up
    docker_wait_mysql_up
    # temporary down
    docker_wait_mysql_down
    # normal up
    docker_wait_mysql_up

    # BGERP init DB connection properties
    export ERP_DB_PWD=$MYSQL_ROOT_PASSWORD
    echo "Setting DB password: '$ERP_DB_PWD'"
    sed -i "s/GENERATED_PASSWORD/$ERP_DB_PWD/" /tmp/bgerp/bgerp.properties
    sed -i "s/GENERATED_PASSWORD/'$ERP_DB_PWD'/" /tmp/bgerp/db_create.sql

    echo "Moving bgerp.properites"
    mv -v /tmp/bgerp/bgerp.properties .

    echo "Creating BGERP user and init database"
    mysql --default-character-set=utf8 -uroot -p$MYSQL_ROOT_PASSWORD < /tmp/bgerp/db_create.sql
else
    # normal up
    docker_wait_mysql_up
fi
