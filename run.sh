#!/bin/bash
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:nativelib/ CLASSPATH=$CLASSPATH:src/main/java/org/meta/plugins java -jar target/jmeta-0.1-jar-with-dependencies.jar
