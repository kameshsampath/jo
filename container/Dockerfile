FROM docker.io/jbangdev/jbang-action

ADD bin/entrypoint.sh /bin/entrypoint

RUN mkdir -p /scripts \
   && chmod -R g+w /scripts \
   && chmod -R g+w /jbang \
   && chgrp -R root /scripts \
   && chgrp -R root /jbang \
   && chmod g+w /etc/passwd \
   && chmod +x /bin/entrypoint


ENTRYPOINT ["entrypoint"]