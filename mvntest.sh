#!/bin/bash
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:nativelib/ mvn clean compile test-compile test
