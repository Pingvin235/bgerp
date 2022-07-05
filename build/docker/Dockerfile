FROM bgerp/base

# bgerp
#======
# java layer removes those with 'apt-get purge'
RUN apt-get update \
	&& apt-get install -y zip unzip wget

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
