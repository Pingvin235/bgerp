echo "Running MariaDB"

docker_wait_mysql_up () {
    for i in {300..0}; do
        if mariadb-admin ping -h localhost --silent; then
            break
        fi
        sleep 1
    done
    if [ "$i" = 0 ]; then
        echo `date`" Unable to start MariaDB server."
        exit 1
    else
        echo `date`" MariaDB started, $i."
    fi
}

docker_wait_mysql_down () {
    for i in {300..0}; do
        if ! mariadb-admin ping -h localhost --silent; then
            break
        fi
        sleep 1
    done
    if [ "$i" = 0 ]; then
        echo `date`" Unable to stop MariaDB server."
        exit 1
    else
        echo `date`" MariaDB stopped, $i."
    fi
}
