# https://github.com/docker-library/mysql/blob/master/8.0/Dockerfile.debian
FROM mysql:8-debian

# mysql
#======
# used for passwords generation
RUN apt-get update && apt-get install -y pwgen

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

# java, taken from: https://github.com/adoptium/containers/blob/main/21/jdk/ubuntu/jammy/Dockerfile.releases.full
#======================================================================================================
ENV JAVA_HOME /opt/java/openjdk
ENV PATH $JAVA_HOME/bin:$PATH

# Default to UTF-8 file.encoding
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends tzdata curl wget ca-certificates fontconfig locales p11-kit binutils \
    && echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
    && locale-gen en_US.UTF-8 \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_VERSION jdk-21+35

RUN set -eux; \
    ARCH="$(dpkg --print-architecture)"; \
    case "${ARCH}" in \
       aarch64|arm64) \
         ESUM='33e440c237438aa2e3866d84ead8d4e00dc0992d98d9fd0ee2fe48192f2dbc4b'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21%2B35/OpenJDK21U-jdk_aarch64_linux_hotspot_21_35.tar.gz'; \
         ;; \
       amd64|i386:x86-64) \
         ESUM='82f64c53acaa045370d6762ebd7441b74e6fda14b464d54d1ff8ca941ec069e6'; \
         BINARY_URL='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21%2B35/OpenJDK21U-jdk_x64_linux_hotspot_21_35.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
      wget -O /tmp/openjdk.tar.gz ${BINARY_URL}; \
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

RUN echo Verifying install ... \
    && fileEncoding="$(echo 'System.out.println(System.getProperty("file.encoding"))' | jshell -s -)"; [ "$fileEncoding" = 'UTF-8' ]; rm -rf ~/.java \
    && echo javac --version && javac --version \
    && echo java --version && java --version \
    && echo Complete.

# bgerp files
#======
RUN mkdir /tmp/bgerp
COPY files/bgerp.properties /tmp/bgerp
COPY files/db_create.sql /tmp/bgerp

COPY docker-bgerp-base.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-bgerp-base.sh
ENTRYPOINT []
