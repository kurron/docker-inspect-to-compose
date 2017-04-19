#!/bin/bash

IMAGE=${1:-kurron/docker-inspect-to-compose:latest}
OUTPUT=${2:-generated-docker-compose.yml}

CMD="docker run \
       --rm \
       --interactive \
       --tty \
       --name docker-to-compose \
       --net "host" \
       --volume $(pwd):/home/microservice \
       --volume /var/run/docker.sock:/var/run/docker.sock \
       --workdir /home/microservice \
       ${IMAGE} --output=/home/microservice/${OUTPUT}"

echo $CMD
$CMD
