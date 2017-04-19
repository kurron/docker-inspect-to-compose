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
A convenience script exists that assembles the application and packages it up as a Docker container.
Simply run `./build.sh` to create the new Docker image.  `./tag-and-push.sh` deploys the image
to the Docker Hub.

# Installation
The application runs standalone and can be conveniently launched via `./run.sh`.  This will pull
down the latest Docker image, run the container, creating`generated-docker-compose.yml` in the
current directory.

# Tips and Tricks

## Specifying The Output File
By default, `generated-docker-compose.yml` is generated. If you want use a different file name,
supply the file name to the `run.sh` script.  For example, `./run.sh foo.yml`.

## Validating The Output
Use something like `docker-compose --file foo.yml config --services` to see if Docker Compose thinks
the generated file is valid or not.

# Troubleshooting

# License and Credits
This project is licensed under the [Apache License Version 2.0, January 2004](http://www.apache.org/licenses/).

# List of Changes
