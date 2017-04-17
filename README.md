# Overview
This project creates a JVM-based tool that will interrogate any Docker containers
installed in the specified Docker Engine and generate a descriptor from that
information.  Supported output forms include:

* Docker Compose v3
* Kubernetes release N
* a neutral format of my own design

# Prerequisites

* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed and working
* Development and testing was done on [Ubuntu Linux](http://www.ubuntu.com/) but other JVM compatible operating systems should work as well

# Building
The project uses [Gradle](http://gradle.org/) to manage builds and will install itself upon first build.  To initiate a build,
simply issue `./gradlew`.  After a moment or two, you should have a fully assembled application in `build/libs`.

# Installation
The application runs standalone and can be conveniently launched via `./gradlew bootRun`.

# Tips and Tricks

# Troubleshooting

# License and Credits
This project is licensed under the [Apache License Version 2.0, January 2004](http://www.apache.org/licenses/).

# List of Changes
