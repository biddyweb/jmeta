#!/bin/bash
rm ./db/*
java -XX:ParallelGCThreads=1 -jar bin/meta-core-0.1.jar
