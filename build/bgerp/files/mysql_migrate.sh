#!/bin/bash

#####################################################################
# Script creates Mariadb DBMS in Docker container,
# then creates and copies BGERP database from the working one,
# creates user with appropriated grants to connect to this database.
# Connection parameters obtain from bgerp.properties file.
#
# Author: Alexander Yaryzhenko
#####################################################################

set -u
set -e

readonly DOCKER=/usr/bin/docker
readonly MARIADB_DOCKER_IMAGE='mariadb:lts'
readonly DB_NAME=bgerp
readonly CONNECT_PORT=33061
readonly CONNECT_PASSWORD='12345'
readonly MDB_CONTAINER_NAME=mariadb
readonly DOCKER_MDB_CLIENT=( "${DOCKER}" exec -t "${MDB_CONTAINER_NAME}" mariadb )
readonly DOCKER_BASH=( "${DOCKER}" exec -t "${MDB_CONTAINER_NAME}" /bin/bash )
readonly MDB_CONN_PARAMS=( --ssl=0 --host 127.0.0.1 -u root -p${CONNECT_PASSWORD} )
readonly MDB_CONF_DIR='/etc/mariadb-docker/mariadb.conf.d'
readonly DB_DIR='/var/lib/mariadb' # for future database files
readonly BGERP_DIR='/opt/bgerp'
readonly BACKUP_DIR="${BGERP_DIR}/tmp" # for dump and backup files
readonly UNZIP='/usr/bin/unzip'
readonly SLEEP=/usr/bin/sleep
CUR_BACKUP_NAME=

function create_mariadb_conf_file() {
mkdir -p ${MDB_CONF_DIR}
cat <<-EOF > "${MDB_CONF_DIR}/server.cnf"
	[mariadbd]
	skip-name-resolve
	sql-mode=
	innodb_file_per_table=1
	innodb_buffer_pool_size=1G (?)
	character-set-server     = utf8mb4
	character-set-collations = utf8mb4=uca1400_ai_ci
EOF
}

function create_db_dump(){
    res=$(${BGERP_DIR}/backup.sh create_db)  # 'Backup name: 2025-03-17_16_45.db.zip'
    CUR_BACKUP_NAME=$(echo $res | cut -d ' ' -f3)
    $UNZIP -p ${BGERP_DIR}/backup/${CUR_BACKUP_NAME} dump.sql > ${BACKUP_DIR}/dump.sql
    echo "Current DB backup file name: ${CUR_BACKUP_NAME}"
}

function run_mariadb_in_docker(){
    echo "Starting MariaDB server in Docker...   "
    if [ -z "$(docker images | sed -n '/mariadb \{1,\}lts/p')" ]; then
        $DOCKER pull ${MARIADB_DOCKER_IMAGE}
    fi

    local DOCKER_PARAMS=( --cap-add=sys_nice --name="${MDB_CONTAINER_NAME}" -e MARIADB_ROOT_PASSWORD="${CONNECT_PASSWORD}" -v "${MDB_CONF_DIR}":/etc/mysql/mariadb.conf.d -v "${DB_DIR}":/var/lib/mysql -v /etc/localtime:/etc/localtime:ro -v "${BACKUP_DIR}":/opt/backups -p 127.0.0.1:${CONNECT_PORT}:3306 -d "${MARIADB_DOCKER_IMAGE}" --ssl=off )
    $DOCKER run "${DOCKER_PARAMS[@]}"

    local TIMEOUT=10
    local SCR="TT=${TIMEOUT}; tt=0; while !  /usr/bin/mariadb-admin ping ${MDB_CONN_PARAMS[@]} >/dev/null 2>&1; do if [ \$tt -ge \$TT ]; then echo Timeout_exceeded ; exit 1; fi; echo Waiting...; $SLEEP 1; ((tt=tt+1)); done;"
    "${DOCKER_BASH[@]}" -c "${SCR}"

    echo "Mariadb is running."
}

function create_and_fill_db(){
    echo "Creating BGERP database...   "
    local MDB_SCRIPT="CREATE DATABASE IF NOT EXISTS ${DB_NAME}; use ${DB_NAME}; source /opt/backups/dump.sql; "
    "${DOCKER_MDB_CLIENT[@]}" "${MDB_CONN_PARAMS[@]}" -e "${MDB_SCRIPT}"
    echo "Done"
}

function add_db_user(){
    local FILE="${BGERP_DIR}"/bgerp.properties
    local PASSWORD=$(grep db.pswd $FILE | cut -d'=' -f2)
    local USER=$(grep db.user $FILE | cut -d'=' -f2)
    local HOST_PORT=$(grep db.url $FILE | cut -d'/' -f3)
    local HOST=$(echo $HOST_PORT | cut -d':' -f1)
    local DB=$(grep db.url $FILE | cut -d'?' -f1 | cut -d'/' -f4)
    local MDB_SCRIPT="DROP USER '${USER}'@'${HOST}'; FLUSH PRIVILEGES; CREATE USER IF NOT EXISTS '${USER}'@'${HOST}' IDENTIFIED BY '${PASSWORD}'; GRANT ALL PRIVILEGES ON ${DB}.* TO '${USER}'@'${HOST}'; FLUSH PRIVILEGES;"

    echo "Adding user for BGERP database...   "
    "${DOCKER_MDB_CLIENT[@]}" "${MDB_CONN_PARAMS[@]}" -e "${MDB_SCRIPT}"
    echo "Done"
}

########################
if [ "$EUID" -ne 0 ]
    then echo "Please run as root"
    exit 1
fi

mkdir -p ${DB_DIR}
mkdir  -p ${BACKUP_DIR}
create_mariadb_conf_file
create_db_dump
run_mariadb_in_docker
create_and_fill_db
add_db_user

