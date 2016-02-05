# JMETA

JMeta is a free software project, written in Java, aiming to provide a platform and a framework for building fully-decentralized P2P search applications.  
JMeta is currently based on [TomP2P](https://github.com/tomp2p/TomP2P), an advanced DHT library.  

More information about the project : [Project concepts](https://gitlab.ouahpiti.info/meta/jmeta/wikis/concepts)

## License

JMeta is released under the terms of the AGPL v3 License.  

## Contributing

JMeta is at a very early stage of development and is under heavy changes.  
But we always need testers, reviewers and hackers to enhance the project and report bugs.  
If you feel excited about the project, please see [How to contribute](contribute)  
See the also the [developers documentation](https://gitlab.ouahpiti.info/meta/jmeta/wikis/dev/developers)

You have an interesting application idea to build upon JMeta ?  
We need you!

Feel free to contact us for any questions, ideas or remarks!  

More on how to add plugins to Jmeta and start building your own P2P-based search app: [Plugin Development](https://gitlab.ouahpiti.info/meta/jmeta/wikis/dev/plugins)  

## Building and installing

The software is not packaged at this time and you need to manually build JMeta.

Dependencies required: Java 8 (openjdk 1.8) and maven.

You also need to have a valid internet connection.

Get the last sources:
`# git -c http.sslVerify=false clone https://gitlab.ouahpiti.info/meta/jmeta.git`

Change to the cloned directory:
`# cd jmeta`

Launch the provided build script to start building: 
`# ./tools/mvnbuild.sh`

The first run will be long as maven needs to download all dependencies.

The executable standalone jar is generated in the 'bin' directory.

To launch Jmeta:
Either use the provided launch script: `# ./tools/run.sh`

Or manually with your own options: `# java -jar ./bin/meta-core-0.1.jar`
