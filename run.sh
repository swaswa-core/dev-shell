#!/bin/bash

# Dev Shell production launcher with minimal logging

# JVM options to silence various startup messages
JVM_OPTS="-Dlogging.level.root=ERROR \
          -Dlogging.level.ch.qos.logback=ERROR \
          -Dspring.main.banner-mode=off \
          -Dspring.jmx.enabled=false \
          -Dspring.main.log-startup-info=false \
          -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"

# Run the application
java $JVM_OPTS -jar target/dev-shell.jar "$@"