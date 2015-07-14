#!/bin/bash
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:nativelib/ mvn clean compile assembly:single
#CLASSPATH=$CLASSPATH:src/main/java/org/meta/plugins
