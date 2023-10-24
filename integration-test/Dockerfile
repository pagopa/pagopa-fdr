FROM python:3.11-slim as build

ARG SHELL="/bin/bash"

COPY ./ /app

WORKDIR /app

RUN apt-get update \
    && apt-get -y install libpq-dev gcc \
    && pip install -r requirements.txt

FROM build as execution
COPY --from=build ./ ./

ENV PSP_SUBSCRIPTION_KEY "AT_RUNTIME"
ENV ORG_SUBSCRIPTION_KEY "AT_RUNTIME"
ENV TAGS "runnable"

ENTRYPOINT ["sh", "run_test.sh"]


