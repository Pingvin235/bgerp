#!/bin/bash

# generated each time, but used only on init first time
export MYSQL_ROOT_PASSWORD="$(pwgen -1 32)"

source docker-bgerp-mysql.sh

# may be used later for startup with regular erp_start.sh
docker_wait_bgerp_up () {
    for i in {300..0}; do
        if /opt/bgerp/erp_status.sh 2>/dev/null; then
            break
        fi
        sleep 1
    done
    if [ "$i" = 0 ]; then
        echo "Unable to start BGERP server."
        exit 1
    fi
}

# $MYSQL_ROOT_PASSWORD will be used there in case of missing DB
# kick off the upstream command
/usr/local/bin/docker-entrypoint.sh mysqld &

source docker-bgerp-data.sh

echo "Running BGERP"
exec /opt/bgerp/erp.sh docker
