# FDR - Flussi di rendicontazione

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-fdr&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-fdr)

Manage FDR ( aka "Flussi di Rendicontazione" ) exchanged between PSP and EC

---

## Api Documentation 📖

- Internal API - See
  the [OpenApi 3 here](https://editor-next.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-fdr/main/openapi/openapi_internal.json)
- Organization API - See
  the [OpenApi 3 here](https://editor-next.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-fdr/main/openapi/openapi_organization.json)
- PSP API - See
  the [OpenApi 3 here](https://editor-next.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-fdr/main/openapi/openapi_psp.json)

In local env typing following url on browser for ui interface:

```http://localhost:8080/q/swagger-ui```

or that for `yaml` version ```http://localhost:8080/q/openapi```

or that for `json` version ```http://localhost:8080/q/openapi?format=json```

---

## Technology Stack 📚

- Java 17 Runtime Environment GraalVM CE
- [Quarkus](https://quarkus.io/)
- quarkus-resteasy-reactive
- quarkus-logging-gelf
- quarkus-micrometer-registry-prometheus
- quarkus-smallrye-health
- quarkus-opentelemetry
- quarkus-smallrye-openapi
- quarkus-resteasy-reactive-jackson
- quarkus-agroal
- quarkus-hibernate-orm-panache
- quarkus-jdbc-postgresql
- quarkus-hibernate-validator
- quarkus-narayana-jta
- lombok (provided)
- mapstruct

---

## Running the infrastructure 🚀

Requirements:

- docker (v20.10.23)
- docker-compose (v2.15.1)

This docker-compose run:

- ELK
    - elasticsearch
    - logstash
    - [kibana](http://localhost:5601/)
- Monitoring
    - alertmanager
    - [prometheus](http://localhost:9090/),
    - [grafana](http://localhost:3000/) (user: ```admin```, password: ```admin```)
- Tracing
    - otel-collector
    - [jaeger](http://localhost:16686/)
- DB
    - Postgres (jdbc: ```jdbc:postgresql://postgres:5432/quarkus```, user: ```admin```,
      password: ```admin```)

```shell script
sh run-local-infra.sh
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

Otherwise, with quarkus CLI:

```
brew install quarkusio/tap/quarkus
quarkus dev -DskipTests=true
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only
> at http://localhost:8080/q/dev/.

## Creating a native executable

You can create a native executable using:

```shell script
sh build-and-run.sh build
```

for run use ```sh build-and-run.sh run```

## Creating updated OpenAPI Swaggers

You can create all updated OpenAPI Swaggers using:

```shell script
sh build-and-run.sh generate_openapi
```

---

# Run Tests 🧪 [WIP] 👩‍💻

#### Unit test

Typing `mvn clean verify`

#### Integration test

- Run the application
- Install dependencies: `yarn install`
- Run the test: `yarn test`

---

## Contributors 👥

Made with ❤️ by PagoPa S.p.A.

### Mainteiners

See `CODEOWNERS` file
