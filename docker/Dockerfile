FROM azul/zulu-openjdk-alpine:11

ADD build/libs/orbit-test-client-release.jar /opt/orbitTest/libs/orbit-test-client.jar
ADD docker/entrypoint.sh /opt/orbit/

RUN ["chmod", "+x", "/opt/orbit/entrypoint.sh"]

CMD ["/opt/orbit/entrypoint.sh"]