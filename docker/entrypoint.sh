#!/usr/bin/env sh

# Start Orbit Test Client
cd /opt/orbitTest
java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 ./libs/orbit-test-client.jar