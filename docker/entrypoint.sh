#!/usr/bin/env sh

# Start Orbit Carnival sample app
cd /opt/orbitCarnival
java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 ./libs/orbit-carnival.jar