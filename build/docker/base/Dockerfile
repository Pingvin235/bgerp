# https://github.com/MariaDB/mariadb-docker/blob/a272347802e1764dd8c0e15ba2b2abfeeadb3bb6/11.4/Dockerfile
FROM mariadb:11.4.7-noble

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

# java, taken from: https://github.com/adoptium/containers/blob/main/21/jdk/ubuntu/noble/Dockerfile
#================================================================================================================
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Default to UTF-8 file.encoding
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN set -eux; \
    apt-get update; \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        # curl required for historical reasons, see https://github.com/adoptium/containers/issues/255
        curl \
        wget \
        # gnupg required to verify the signature
        gnupg \
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

ENV JAVA_VERSION=jdk-21.0.7+6

RUN set -eux; \
    ARCH="$(dpkg --print-architecture)"; \
    case "${ARCH}" in \
       amd64) \
         ESUM='974d3acef0b7193f541acb61b76e81670890551366625d4f6ca01b91ac152ce0'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_6.tar.gz'; \
         ;; \
       arm64) \
         ESUM='31dba70ba928c78c20d62049ac000f79f7a7ab11f9d9c11e703f52d60aa64f93'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.7_6.tar.gz'; \
         ;; \
       ppc64el) \
         ESUM='2ddc0dc14b07d9e853875aac7f84c23826fff18b9cea618c93efe0bcc8f419c2'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_ppc64le_linux_hotspot_21.0.7_6.tar.gz'; \
         ;; \
       riscv64) \
         ESUM='d75f33ee7f9e5532bce263db83443ffed7d9bae7ff3ed41e48d137808adfe513'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_riscv64_linux_hotspot_21.0.7_6.tar.gz'; \
         ;; \
       s390x) \
         ESUM='216edbccab2d677639c90d2cb09dfa39c257788a62b8b83d879528c447b9ad33'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_s390x_linux_hotspot_21.0.7_6.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
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
RUN apt-get update && apt-get install -y openssh-client rsync git-lfs zip unzip

# bgerp files
#============
RUN mkdir /tmp/bgerp
COPY files/bgerp.properties /tmp/bgerp
COPY files/db_create.sql /tmp/bgerp

COPY docker-bgerp-base.sh docker-bgerp-mysql.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-bgerp-base.sh
ENTRYPOINT []
