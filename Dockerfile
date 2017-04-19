FROM azul/zulu-openjdk:latest

MAINTAINER Ron Kurr "kurr@jvmguy.com"

CMD ["--output=/home/microservice/baz.yml"]

ENTRYPOINT ["java", \
            "-server", \
            "-XX:+UseSerialGC", \
            "-XX:+ScavengeBeforeFullGC", \
            "-XX:+CMSScavengeBeforeRemark", \
            "-XX:MinHeapFreeRatio=20", \
            "-XX:MaxHeapFreeRatio=40", \
            "-XX:MaxRAM=128m", \
            "-Djava.awt.headless=true", \
            "-Dsun.net.inetaddr.ttl=60", \
            "-jar", \
            "/opt/inspector.jar"]

COPY build/libs/docker-inspect-to-compose-0.0.0.RELEASE-executable.jar /opt/inspector.jar
