echo "Running MySQL"

docker_wait_mysql_up () {
    for i in {300..0}; do
        if mysqladmin ping -h localhost --silent; then
            break
        fi
        sleep 1
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
