#!/bin/bash
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:nativelib/ CLASSPATH=$CLASSPATH:src/main/java/org/meta/plugins mvn test
