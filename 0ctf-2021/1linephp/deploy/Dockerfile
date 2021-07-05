FROM php:7.4.11-apache

RUN sed -i 's/deb.debian.org/mirror.sjtu.edu.cn/g' /etc/apt/sources.list && \
    sed -i 's/security.debian.org/mirror.sjtu.edu.cn/g' /etc/apt/sources.list && \
    apt-get update -y && \
    apt-get install libzip-dev -y && \
    docker-php-ext-install zip && \
    mkdir /dd810fc36330c200a_flag

COPY source /var/www/html
COPY flag /dd810fc36330c200a_flag
