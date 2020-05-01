FROM azul/zulu-openjdk-alpine:11

ADD build/libs/orbit-test-client-release.jar /opt/orbitTest/libs/orbit-test-client.jar
ADD docker/entrypoint.sh /opt/orbitTest/

RUN ["chmod", "+x", "/opt/orbitTest/entrypoint.sh"]

CMD ["/opt/orbitTest/entrypoint.sh"]