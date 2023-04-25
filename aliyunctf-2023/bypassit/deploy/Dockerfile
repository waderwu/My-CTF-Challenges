FROM openjdk:8u332-jdk

RUN mkdir /opt/app
RUN mkdir /opt/rasp

COPY bypassit.jar /opt/app
COPY flag /flag
COPY readflag /readflag

RUN chmod 400 /flag
RUN chmod u+s /readflag

COPY naiverasp.jar /opt/rasp
COPY libnativerasp.so /opt/rasp

WORKDIR /opt/app

USER nobody
CMD ["java", "-javaagent:/opt/rasp/naiverasp.jar", "-jar", "/opt/app/bypassit.jar"]