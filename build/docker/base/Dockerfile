# https://github.com/docker-library/mysql/blob/master/8.0/Dockerfile.debian
FROM mysql:8-debian

# mysql
#======
# used for passwords generation
RUN apt-get update --allow-insecure-repositories && apt-get install -y pwgen

# logging dir
RUN mkdir /var/log/mysql; chown mysql:mysql /var/log/mysql

# my.cnf parameters: empty sql-mode, logs
RUN sed -i '/\[mysqld\]/a sql-mode= ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a log_error=/var/log/mysql/mysql_error.log ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a general_log_file=/var/log/mysql/mysql.log ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a general_log=0 ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a slow_query_log_file=/var/log/mysql/mysql_slow.log ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a slow_query_log=1 ' /etc/mysql/my.cnf;

# avoids errors with 'apt-get update' running after
RUN rm /etc/apt/sources.list.d/mysql.list

# java, taken from: https://github.com/adoptium/containers/blob/main/21/jdk/ubuntu/jammy/Dockerfile
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

ENV JAVA_VERSION=jdk-21.0.4+7

RUN set -eux; \
    ARCH="$(dpkg --print-architecture)"; \
    case "${ARCH}" in \
       amd64) \
         ESUM='51fb4d03a4429c39d397d3a03a779077159317616550e4e71624c9843083e7b9'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.4_7.tar.gz'; \
         ;; \
       arm64) \
         ESUM='d768eecddd7a515711659e02caef8516b7b7177fa34880a56398fd9822593a79'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_aarch64_linux_hotspot_21.0.4_7.tar.gz'; \
         ;; \
       ppc64el) \
         ESUM='c208cd0fb90560644a90f928667d2f53bfe408c957a5e36206585ad874427761'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_ppc64le_linux_hotspot_21.0.4_7.tar.gz'; \
         ;; \
       s390x) \
         ESUM='c900c8d64fab1e53274974fa4a4c736a5a3754485a5c56f4947281480773658a'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_s390x_linux_hotspot_21.0.4_7.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
    wget --progress=dot:giga -O /tmp/openjdk.tar.gz ${BINARY_URL}; \
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
