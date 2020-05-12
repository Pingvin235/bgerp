#!/bin/bash

# generated each time, but used only on init first time
export MYSQL_ROOT_PASSWORD="$(pwgen -1 32)"

echo "Running MySQL"

docker_wait_mysql_up () {
    for i in {30..0}; do
        if mysqladmin ping -h localhost --silent; then
            break
        fi
        sleep 1
    done
    if [ "$i" = 0 ]; then
        echo "Unable to start MySQL server."
        exit 1
    fi
}

docker_wait_mysql_down () {
    for i in {30..0}; do
        if ! mysqladmin ping -h localhost --silent; then
            break
        fi
        sleep 1
    done
    if [ "$i" = 0 ]; then
        echo "Unable to stop MySQL server."
        exit 1
    fi
}

# $MYSQL_ROOT_PASSWORD will be used there in case of missing DB
# kick off the upstream command
usr/local/bin/docker-entrypoint.sh mysqld & 

if [ ! -d "/var/lib/mysql/mysql" ]; then
    echo "MySQL data directory init"

    # temporary up
    docker_wait_mysql_up
    # temporary down
    docker_wait_mysql_down
    # normal up
    docker_wait_mysql_up

    # BGERP init DB
    cp /opt/bgerp/bgerp.properties /opt/bgerp/conf

    export ERP_DB_PWD=`pwgen`
    echo "Setting DB password: '$ERP_DB_PWD'"
    sed -i "s/GENERATED_PASSWORD/$ERP_DB_PWD/" /opt/bgerp/conf/bgerp.properties
    sed -i "s/GENERATED_PASSWORD/'$ERP_DB_PWD'/" /opt/bgerp/db_create.sql

    echo "Create BGERP user and init database"
    mysql --default-character-set=utf8 -uroot -p$MYSQL_ROOT_PASSWORD < /opt/bgerp/db_create.sql
    mysql --default-character-set=utf8 -ubgerp -p$ERP_DB_PWD bgerp < /opt/bgerp/db_init.sql
    
    echo "Apply DEMO database"
    mysql --default-character-set=utf8 -ubgerp -p$ERP_DB_PWD bgerp < /opt/bgerp/bgerp.sql
    echo "Apply DEMO filestorage"
    unzip /opt/bgerp/filestorage.zip -d /opt/bgerp/filestorage
else
    # normal up
    docker_wait_mysql_up
fi

echo "Running BGERP"
cp -r /opt/bgerp/conf/bgerp.properties /opt/bgerp/bgerp.properties
#sed -i "s/JAVA_HOME=/JAVA_HOME=\"\$JAVA_HOME\"/" /opt/bgerp/setenv.sh
# for DB updates only
/opt/bgerp/installer.sh install update.zip
exec /opt/bgerp/erp.sh docker
