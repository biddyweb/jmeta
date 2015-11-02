Quick notes about the project
=============================

Meta aims to provide a platform and a framework for building p2p applications

This is a java implementation of the concept behind it : jmeta

The tools provided let developers build sorts of search engines upon a dht,
enabling users collaboration.

This is a very early stage of development.

Note for developers:
====================


* jkyotocabinet compilation

jkyotocabinet sources available from : 
http://fallabs.com/kyotocabinet/javapkg/

On debian, to be able to ./configure, you'll need to tell the script about
environment :

MYJAVAHOME=/usr/lib/jvm/java-7-openjdk-amd64/ ./configure

Then : make install


* jni

The JNI kyotocabinet binding is a bit tricky to make it work in a portable
fashion. What is done at the moment is to link to a local directory and put
symbolic links in it:

ln -s /usr/local/lib/libjkyotocabinet.so nativelib/libjkyotocabinet.so

(make sure to adapt /usr/local/lib to your local installation directory)


* Building jar file and executing

To compile jar file, with dependency : 

mvn clean compile assembly:single

To launch jar file, from base dir:

LD_LIBRARY_PATH=$LD_LIBRARY_PATH:nativelib/ CLASSPATH=$CLASSPATH:src/main/java/org/meta/plugins java -jar bin/meta-core-0.1.jar
