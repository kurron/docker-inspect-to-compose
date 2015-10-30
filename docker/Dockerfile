# Pre-built JDK 8 image
FROM kurron/docker-oracle-jdk-8:latest

MAINTAINER Ron Kurr <kurr@jvmguy.com>

# copy the application jar file from the build output directory into the image
COPY artifacts/jvm-guy-rest-api.jar /opt/example/jvm-guy-rest-api.jar

# expose the port that the application will be listening on
EXPOSE 8080

# set the working directory to where the application jar file was copied
WORKDIR /opt/example

# start application - Example usage: docker run --detach --publish 8090:8080 --name new-container-instance <image name> --spring.profiles.active=integration
ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "jvm-guy-rest-api.jar"]
