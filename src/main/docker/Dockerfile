FROM docker.io/jbangdev/jbang-action

ADD bin/entrypoint.sh /bin/entrypoint

ENV SCRIPTS_HOME /scripts

RUN useradd -u 10001 -r -g 0 -m \
     -d ${SCRIPTS_HOME} -s /sbin/nologin -c "Jo user" jo \
   && chmod -R g+w /scripts \
   && chmod -R g+w /jbang \
   && chgrp -R root /scripts \
   && chgrp -R root /jbang \
   && chmod g+w /etc/passwd \
   && chmod +x /bin/entrypoint

USER 10001

ENV PATH="${PATH}:/jbang/bin"

ENTRYPOINT ["entrypoint"]
CMD ["--help"]