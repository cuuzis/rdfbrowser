# config has only changed port to 80
# src is the folder with war files
#
# build container (dot is current directory for dockerfile):
# docker build -t hello-world .
#
# run container on port 80:
# docker run -p 80:80 hello-world
#
# run shell in docker (parallel to apache process):
# docker exec -t -i [container-name] /bin/bash

FROM tomcat:8.0-jre8
COPY src/ /usr/local/tomcat/webapps
COPY server.xml /usr/local/tomcat/conf/server.xml
EXPOSE 80