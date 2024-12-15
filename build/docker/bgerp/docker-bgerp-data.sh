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
    sed -i "s/GENERATED_PASSWORD/$ERP_DB_PWD/" /opt/bgerp/bgerp.properties
    sed -i "s/GENERATED_PASSWORD/'$ERP_DB_PWD'/" /opt/bgerp/db_create.sql

    # disable scheduler
    echo -e "\nscheduler.start=0" >> /opt/bgerp/bgerp.properties

    echo "Creating BGERP user and init database"
    mysql --default-character-set=utf8 -uroot -p$MYSQL_ROOT_PASSWORD < /opt/bgerp/db_create.sql
    mysql --default-character-set=utf8 -ubgerp -p$ERP_DB_PWD bgerp < /opt/bgerp/db_init.sql

    if [ "$MASTER" != "no" ]; then
        echo "Installing Master release"
        /opt/bgerp/installer.sh installc 0
    else
        echo "Skipping installation Master release"
    fi

    TMP_DIR=/tmp/bgerp

    if [ "$DEMO" != "no" ]; then
        echo "Applying DEMO database"
        wget https://demo.bgerp.org/bgerp.sql -O $TMP_DIR/bgerp.sql
        mysql --default-character-set=utf8 -ubgerp -p$ERP_DB_PWD bgerp < $TMP_DIR/bgerp.sql

        echo "Applying DEMO filestorage"
        wget https://demo.bgerp.org/filestorage.zip -O $TMP_DIR/filestorage.zip
        unzip $TMP_DIR/filestorage.zip -d /opt/bgerp/filestorage
    else
        echo "Skipping DEMO database and filestorage"
    fi

    echo "Downloading DEMO license"
    wget https://bgerp.org/download/lic.data -O /opt/bgerp/lic.data
else
    # normal up
    docker_wait_mysql_up
fi