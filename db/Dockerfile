FROM mysql:8.0

COPY init.sql /docker-entrypoint-initdb.d/init.sql

RUN chmod 644 /docker-entrypoint-initdb.d/init.sql