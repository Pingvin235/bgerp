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
        echo "Unable to start MySQL server."
        exit 1
    fi
}

docker_wait_mysql_down () {
    for i in {300..0}; do
        if ! mysqladmin ping -h localhost --silent; then
            break
        fi
        sleep 2
    done
    if [ "$i" = 0 ]; then
        echo "Unable to stop MySQL server."
        exit 1
    fi
}

# may be used later for startup with regular erp_start.sh
docker_wait_bgerp_up () {
    for i in {300..0}; do
        if /opt/bgerp/erp_status.sh 2>/dev/null; then
            break
        fi
        sleep 2
    done
    if [ "$i" = 0 ]; then
        echo "Unable to start BGERP server."
        exit 1
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
    export ERP_DB_PWD=`pwgen`
    echo "Setting DB password: '$ERP_DB_PWD'"
    sed -i "s/GENERATED_PASSWORD/$ERP_DB_PWD/" /opt/bgerp/bgerp.properties
    sed -i "s/GENERATED_PASSWORD/'$ERP_DB_PWD'/" /opt/bgerp/db_create.sql

    # disable scheduler
    echo -e "\nscheduler.start=0" >> /opt/bgerp/bgerp.properties

    echo "Creating BGERP user and init database"
    mysql --default-character-set=utf8 -uroot -p$MYSQL_ROOT_PASSWORD < /opt/bgerp/db_create.sql
    mysql --default-character-set=utf8 -ubgerp -p$ERP_DB_PWD bgerp < /opt/bgerp/db_init.sql

    echo "Applying DEMO database"
    mysql --default-character-set=utf8 -ubgerp -p$ERP_DB_PWD bgerp < /opt/bgerp/bgerp.sql
    echo "Applying DEMO filestorage"
    unzip /opt/bgerp/filestorage.zip -d /opt/bgerp/filestorage
    echo "Downloading DEMO license https://demo.bgerp.org/lic.data"
    wget https://demo.bgerp.org/lic.data -O /opt/bgerp/lic.data
else
    # normal up
    docker_wait_mysql_up
fi

echo "Running BGERP"
exec /opt/bgerp/erp.sh docker
