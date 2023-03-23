#!/bin/bash

action=$1

if [ -z "$action" ]; then
  echo "Missed action: <build|run|generate_openapi|test_curl>"
  exit 0
fi

REPO=pagopa/pagopafdr

build () {
  conf=$1
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Build version [$version] [$conf]"
  #./mvnw clean package -Pnative -Dquarkus.native.container-build=true -Dquarkus.profile=$conf
  #docker build -f src/main/docker/Dockerfile.native -t $REPO:$version-$conf .
  docker build -f src/main/docker/Dockerfile.multistage --build-arg QUARKUS_PROFILE=$conf -t $REPO:$version-$conf .
}

run () {
  conf=$1
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Run version [$version] [$conf]"
  docker run -i --rm --network=docker-infra_infra -p 8080:8080 $REPO:$version-$conf
}

generate_openapi () {
  conf=$1
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Generate OpenAPI JSON [$version] [$conf]"
  docker run -i -d --name exportopenapifdr --rm -p 8080:8080 $REPO:$version-$conf
  curl http://localhost:8080/q/openapi?format=json > openapi/openapi.json
  docker rm -f exportopenapifdr
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

if echo "build run generate_openapi test_curl" | grep -w $action > /dev/null; then
  if [ $action = "build" ]; then
    build docker
  elif [ $action = "run" ]; then
    echo "###########"
    echo "# REQUIRED: run-local-infra.sh"
    echo "###########"
    run docker
  elif [ $action = "generate_openapi" ]; then
    build openapi
    generate_openapi openapi
  else
    test_curl
  fi
else
    echo "Action [$action] not allowed."
    exit 1
fi
