FROM bgerp/base

# bgerp
#======
COPY files/bgerp.zip /tmp

RUN unzip /tmp/bgerp.zip -d /opt \
	&& rm /tmp/bgerp.zip \
	&& chmod 744 /opt/bgerp/*.sh \
	&& mkdir /opt/bgerp/conf \
	&& mkdir /opt/bgerp/backup

VOLUME /opt/bgerp/filestorage
VOLUME /opt/bgerp/backup
VOLUME /opt/bgerp/conf
VOLUME /opt/bgerp/log

EXPOSE 9088

COPY docker-bgerp.sh docker-bgerp-data.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-bgerp.sh
ENTRYPOINT ["docker-bgerp.sh"]
