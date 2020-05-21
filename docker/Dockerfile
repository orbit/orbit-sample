FROM azul/zulu-openjdk-alpine:11

ADD build/libs/orbit-carnival-release.jar /opt/orbitCarnival/libs/orbit-carnival.jar
ADD docker/entrypoint.sh /opt/orbitCarnival/

RUN ["chmod", "+x", "/opt/orbitCarnival/entrypoint.sh"]

CMD ["/opt/orbitCarnival/entrypoint.sh"]