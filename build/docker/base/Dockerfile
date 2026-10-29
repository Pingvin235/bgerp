# https://github.com/MariaDB/mariadb-docker/blob/master/12.3/Dockerfile
FROM mariadb:12.3.3-noble

# mysql
#======
# used for passwords generation
RUN apt-get update --allow-insecure-repositories && apt-get install -y pwgen

# logging dir
RUN mkdir /var/log/mysql; chown mysql:mysql /var/log/mysql

# my.cnf parameters: empty sql-mode, logs
RUN sed -i '/\[mariadbd\]/a sql-mode= ' /etc/mysql/mariadb.conf.d/50-server.cnf; \
	sed -i '/\[mariadbd\]/a log_error=/var/log/mysql/mysql_error.log ' /etc/mysql/mariadb.conf.d/50-server.cnf; \
	sed -i '/\[mariadbd\]/a general_log_file=/var/log/mysql/mysql.log ' /etc/mysql/mariadb.conf.d/50-server.cnf; \
	sed -i '/\[mariadbd\]/a general_log=0 ' /etc/mysql/mariadb.conf.d/50-server.cnf; \
	sed -i '/\[mariadbd\]/a slow_query_log_file=/var/log/mysql/mysql_slow.log ' /etc/mysql/mariadb.conf.d/50-server.cnf; \
	sed -i '/\[mariadbd\]/a slow_query_log=1 ' /etc/mysql/mariadb.conf.d/50-server.cnf; \
  sed -i '/\[mariadbd\]/a innodb_use_native_aio=OFF ' /etc/mysql/mariadb.conf.d/50-server.cnf;

# avoids errors with 'apt-get update' running after
RUN rm /etc/apt/sources.list.d/mariadb.list

# java, taken from: https://github.com/adoptium/containers/blob/main/25/jdk/ubuntu/noble/Dockerfile
#================================================================================================================
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Default to UTF-8 file.encoding
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN set -eux; \
    apt-get update; \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        # java.lang.UnsatisfiedLinkError: libfontmanager.so: libfreetype.so.6: cannot open shared object file: No such file or directory
        # java.lang.NoClassDefFoundError: Could not initialize class sun.awt.X11FontManager
        # https://github.com/docker-library/openjdk/pull/235#issuecomment-424466077
        fontconfig \
        # utilities for keeping Ubuntu and OpenJDK CA certificates in sync
        # https://github.com/adoptium/containers/issues/293
        ca-certificates p11-kit \
        # jlink --strip-debug on 13+ needs objcopy: https://github.com/docker-library/openjdk/issues/351
        # Error: java.io.IOException: Cannot run program "objcopy": error=2, No such file or directory
        binutils \
        tzdata \
        # locales ensures proper character encoding and locale-specific behaviors using en_US.UTF-8
        locales \
    ; \
    echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen; \
    locale-gen en_US.UTF-8; \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_VERSION=jdk-25.0.5+8

RUN set -eux; \
    ARCH="$(dpkg --print-architecture)"; \
    case "${ARCH}" in \
        amd64) \
            ESUM='68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb'; \
            BINARY_URL='https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.5%2B8/OpenJDK25U-jdk_x64_linux_hotspot_25.0.5_8.tar.gz'; \
            ;; \
        *) \
            echo "Unsupported arch: ${ARCH}"; \
            exit 1; \
            ;; \
    esac; \
    savedAptMark="$(apt-mark showmanual)"; \
    apt-get update; \
    apt-get install -y --no-install-recommends wget gnupg; \
    wget --progress=dot:giga -O /tmp/openjdk.tar.gz ${BINARY_URL}; \
    wget --progress=dot:giga -O /tmp/openjdk.tar.gz.sig ${BINARY_URL}.sig; \
    export GNUPGHOME="$(mktemp -d)"; \
    # gpg: key 843C48A565F8F04B: "Adoptium GPG Key (DEB/RPM Signing Key) <temurin-dev@eclipse.org>" imported
    gpg --batch --keyserver keyserver.ubuntu.com --recv-keys 3B04D753C9050D9A5D343F39843C48A565F8F04B; \
    gpg --batch --verify /tmp/openjdk.tar.gz.sig /tmp/openjdk.tar.gz; \
    rm -rf "${GNUPGHOME}" /tmp/openjdk.tar.gz.sig; \
    echo "${ESUM} */tmp/openjdk.tar.gz" | sha256sum -c -; \
    mkdir -p "$JAVA_HOME"; \
    tar --extract \
        --file /tmp/openjdk.tar.gz \
        --directory "$JAVA_HOME" \
        --strip-components 1 \
        --no-same-owner \
    ; \
    rm -f /tmp/openjdk.tar.gz ${JAVA_HOME}/lib/src.zip; \
    apt-mark auto '.*' > /dev/null; \
    apt-mark manual $savedAptMark > /dev/null; \
    apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false; \
    rm -rf /var/lib/apt/lists/*; \
    # https://github.com/docker-library/openjdk/issues/331#issuecomment-498834472
    find "$JAVA_HOME/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf; \
    ldconfig; \
    # https://github.com/docker-library/openjdk/issues/212#issuecomment-420979840
    # https://openjdk.java.net/jeps/341
    java -Xshare:dump;

RUN set -eux; \
    echo "Verifying install ..."; \
    fileEncoding="$(echo 'System.out.println(System.getProperty("file.encoding"))' | jshell -s -)"; [ "$fileEncoding" = 'UTF-8' ]; rm -rf ~/.java; \
    echo "javac --version"; javac --version; \
    echo "java --version"; java --version; \
    echo "Complete."

# gitlab ci, bgerp
#=================
RUN apt-get update && apt-get install -y openssh-client rsync git-lfs zip unzip wget

# bgerp files
#============
RUN mkdir /tmp/bgerp
COPY files/bgerp.properties /tmp/bgerp
COPY files/db_create.sql /tmp/bgerp

COPY docker-bgerp-base.sh docker-bgerp-mysql.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-bgerp-base.sh
ENTRYPOINT []
