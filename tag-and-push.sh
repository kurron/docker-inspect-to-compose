#!/bin/bash

# use the time as a tag
UNIXTIME=$(date +%s)

# docker tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]
docker tag dockerinspecttocompose_docker-inspect-to-compose:latest kurron/docker-inspect-to-compose:latest
docker tag dockerinspecttocompose_docker-inspect-to-compose:latest kurron/docker-inspect-to-compose:${UNIXTIME}
docker images

# Usage:  docker push [OPTIONS] NAME[:TAG]
docker push kurron/docker-inspect-to-compose:latest
docker push kurron/docker-inspect-to-compose:${UNIXTIME}
