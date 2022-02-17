# https://github.com/docker-library/mysql/blob/master/8.0/Dockerfile.debian
FROM mysql:8

# mysql
#======
# logging dir
RUN mkdir /var/log/mysql; chown mysql:mysql /var/log/mysql

# my.cnf parameters: empty sql-mode, logs
RUN sed -i '/\[mysqld\]/a sql-mode= ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a log_error=/var/log/mysql/mysql_error.log ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a general_log_file=/var/log/mysql/mysql.log ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a general_log=0 ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a slow_query_log_file=/var/log/mysql/mysql_slow.log ' /etc/mysql/my.cnf; \
	sed -i '/\[mysqld\]/a slow_query_log=1 ' /etc/mysql/my.cnf;

# java, taken from: https://github.com/docker-library/openjdk/blob/master/17/jdk/slim-buster/Dockerfile
#======================================================================================================
RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
# utilities for keeping Debian and OpenJDK CA certificates in sync
		ca-certificates p11-kit \
	; \
	rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/local/openjdk-17
RUN { echo '#/bin/sh'; echo 'echo "$JAVA_HOME"'; } > /usr/local/bin/docker-java-home && chmod +x /usr/local/bin/docker-java-home && [ "$JAVA_HOME" = "$(docker-java-home)" ] # backwards compatibility
ENV PATH $JAVA_HOME/bin:$PATH

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

# https://jdk.java.net/
# >
# > Java Development Kit builds, from Oracle
# >
ENV JAVA_VERSION 17.0.2

RUN set -eux; \
	\
	arch="$(dpkg --print-architecture)"; \
	case "$arch" in \
		'amd64') \
			downloadUrl='https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz'; \
			downloadSha256='0022753d0cceecacdd3a795dd4cea2bd7ffdf9dc06e22ffd1be98411742fbb44'; \
			;; \
		'arm64') \
			downloadUrl='https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-aarch64_bin.tar.gz'; \
			downloadSha256='13bfd976acf8803f862e82c7113fb0e9311ca5458b1decaef8a09ffd91119fa4'; \
			;; \
		*) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; \
	esac; \
	\
	savedAptMark="$(apt-mark showmanual)"; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		wget \
	; \
	rm -rf /var/lib/apt/lists/*; \
	\
	wget --progress=dot:giga -O openjdk.tgz "$downloadUrl"; \
	echo "$downloadSha256 *openjdk.tgz" | sha256sum --strict --check -; \
	\
	mkdir -p "$JAVA_HOME"; \
	tar --extract \
		--file openjdk.tgz \
		--directory "$JAVA_HOME" \
		--strip-components 1 \
		--no-same-owner \
	; \
	rm openjdk.tgz*; \
	\
	apt-mark auto '.*' > /dev/null; \
	[ -z "$savedAptMark" ] || apt-mark manual $savedAptMark > /dev/null; \
	apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false; \
	\
# update "cacerts" bundle to use Debian's CA certificates (and make sure it stays up-to-date with changes to Debian's store)
# see https://github.com/docker-library/openjdk/issues/327
#     http://rabexc.org/posts/certificates-not-working-java#comment-4099504075
#     https://salsa.debian.org/java-team/ca-certificates-java/blob/3e51a84e9104823319abeb31f880580e46f45a98/debian/jks-keystore.hook.in
#     https://git.alpinelinux.org/aports/tree/community/java-cacerts/APKBUILD?id=761af65f38b4570093461e6546dcf6b179d2b624#n29
	{ \
		echo '#!/usr/bin/env bash'; \
		echo 'set -Eeuo pipefail'; \
		echo 'trust extract --overwrite --format=java-cacerts --filter=ca-anchors --purpose=server-auth "$JAVA_HOME/lib/security/cacerts"'; \
	} > /etc/ca-certificates/update.d/docker-openjdk; \
	chmod +x /etc/ca-certificates/update.d/docker-openjdk; \
	/etc/ca-certificates/update.d/docker-openjdk; \
	\
# https://github.com/docker-library/openjdk/issues/331#issuecomment-498834472
	find "$JAVA_HOME/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf; \
	ldconfig; \
	\
# https://github.com/docker-library/openjdk/issues/212#issuecomment-420979840
# https://openjdk.java.net/jeps/341
	java -Xshare:dump; \
	\
# basic smoke test
	fileEncoding="$(echo 'System.out.println(System.getProperty("file.encoding"))' | jshell -s -)"; [ "$fileEncoding" = 'UTF-8' ]; rm -rf ~/.java; \
	javac --version; \
	java --version

# bgerp
#======
# java layer removes those with 'apt-get purge'
RUN apt-get update \
	&& apt-get install -y unzip wget

COPY files/bgerp.zip /tmp

RUN unzip /tmp/bgerp.zip -d /opt \
	&& rm /tmp/bgerp.zip \
	&& chmod 744 /opt/bgerp/*.sh \
	&& mkdir /opt/bgerp/conf

RUN mkdir /opt/bgerp/backup

# demo
COPY files/bgerp.sql /opt/bgerp
COPY files/filestorage.zip /opt/bgerp

VOLUME /opt/bgerp/filestorage
VOLUME /opt/bgerp/backup
VOLUME /opt/bgerp/conf
VOLUME /opt/bgerp/log

EXPOSE 9088

COPY docker-bgerp.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-bgerp.sh
ENTRYPOINT ["docker-bgerp.sh"]
