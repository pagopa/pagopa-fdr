#!/bin/bash

action=$1

if [ -z "$action" ]; then
  echo "Missed action: <build|run|test_curl>"
  exit 0
fi

build () {
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Build version [$version]"
  ./mvnw clean package -Pnative -Dquarkus.native.container-build=true -Dquarkus.profile=docker
  docker build -f src/main/docker/Dockerfile.native -t quarkus-quickstart/fdr:$version .
}

run () {
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Build version [$version]"
  docker run -i --rm --network=dev-env_infra -p 8080:8080 quarkus-quickstart/fdr:$version
}

test_curl () {
  curl localhost:8080/q/dev
  curl localhost:8080/q/health
  curl localhost:8080/q/health/live
  curl localhost:8080/q/health/ready
  curl localhost:8080/q/metrics
  curl localhost:8080/q/swagger-ui
  curl localhost:8080/q/openapi
}

if echo "build run test_curl" | grep -w $action > /dev/null; then
  if [ $action = "build" ]; then
    build
  elif [ $action = "run" ]; then
    run
  else
    test_curl
  fi
else
    echo "Action [$action] not allowed."
    exit 1
fi
