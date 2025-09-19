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

  ##Attenzione si usa il file Dockerfile.multistage.jvm, e non Dockerfile.multistage, perchè la lib di azure non è compatibile per la build nativa
  docker build -f src/main/docker/Dockerfile.multistage.jvm \
  --build-arg APP_NAME=pagopafdr --build-arg QUARKUS_PROFILE=$conf \
  -t $REPO:$version-$conf .
}

run () {
  conf=$1
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Run version [$version] [$conf]"
  docker run -i --rm --network=docker-infra_infra -p 8080:8080 $REPO:$version-$conf
}

generate_openapi () {
  conf=$1
  folder_name=$2
  section=$3
  tags=$4
  version=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
  echo "Generate OpenAPI JSON [$version] [$conf]"
  docker run -i -d --name exportfdr_$conf --rm -p 8080:8080 $REPO:$version-$conf
  sleep 10
  if [ $folder_name = "all" ]; then
    curl http://localhost:8080/q/openapi?format=json > openapi/$conf.json
  else
    curl http://localhost:8080/q/openapi?format=json > openapi/$conf.json

    jq --arg tags "$tags" --arg section "$section" '
      walk(
        if type == "object" then
          with_entries(if .key == "examples" then .key = "example" else . end)
          | del(.info.description, .requestBody.required, .exclusiveMinimum, .get.description, .post.description, .put.description, .delete.description)
        else . end
      )
    ' openapi/$conf.json > infra/api/$folder_name/openapi_temp.json

    jq --arg tags "$tags" --arg section "$section" '
          # Converte la stringa separata da virgola in un array
          ($tags | split(",")) as $tagsArray |

          # Elimina tutte le API che non appartengono ai tag specificati (necessario per APIM)
          walk(
            if type == "object" then
              if has("paths") then
                .paths |= with_entries(
                  select(
                    # Controlla se il tag è presente in qualsiasi metodo (GET, POST, PUT, DELETE)
                    any(.value.get.tags[]?; index($tagsArray[]) != null) or
                    any(.value.post.tags[]?; index($tagsArray[]) != null) or
                    any(.value.put.tags[]?; index($tagsArray[]) != null) or
                    any(.value.delete.tags[]?; index($tagsArray[]) != null)
                  )
                )
              else . end
            else . end
          ) |

          # Sostituisce il tag "openapi" dalla versione "3.1.0" alla versione "3.0.1" (necessario per le OpEx)
          walk(
            if type == "object" and has("openapi") and .openapi == "3.1.0" then
              .openapi = "3.0.1"
            else . end
          ) |

          # Sostituisce il tag "url" in tag server
          walk(
            if type == "object" and has("servers") then
                .servers |= map(if .url | contains("{host}") then .url = "${host}" | del(.variables) else . end)
              else .
              end
          ) |

          # Sostituisce il tag "title" inglobando il nome della sezione di API
          walk(
            if type == "object" then
              if has("info") then
                .info.title = "FDR - Flussi di Rendicontazione (\($section))"
              else . end
            else . end
          )
        ' infra/api/$folder_name/openapi_temp.json  > infra/api/$folder_name/openapi.json

    rm infra/api/$folder_name/openapi_temp.json
    rm openapi/$conf.json
  fi
  docker rm -f exportfdr_$conf
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

set -e
if echo "build run generate_openapi test_curl" | grep -w $action > /dev/null; then
  if [ $action = "build" ]; then
    build docker
  elif [ $action = "run" ]; then
    echo "###########"
    echo "# REQUIRED: run-local-infra.sh"
    echo "###########"
    run docker
  elif [ $action = "generate_openapi" ]; then
    build openapi_internal
    generate_openapi openapi_internal internal INTERNAL 'Info,Internal Operations,Support'

    build openapi_psp
    generate_openapi openapi_psp psp PSPs 'Info,PSP'

    build openapi_organization
    generate_openapi openapi_organization org ORGs 'Info,Organizations'

    build openapi
    generate_openapi openapi all
  else
    test_curl
  fi
else
    echo "Action [$action] not allowed."
    exit 1
fi
