# sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <script-name> <subkey>

ENVIRONMENT=$1
TYPE=$2
SCRIPT=$3
DB_NAME=$4
API_SUBSCRIPTION_KEY=$5
PAYMENTS_IN_FLOW=$6
MAX_PARALLEL_CALLS=$7

if [ -z "$ENVIRONMENT" ]
then
  echo "No env specified: sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <db-name> <subkey> <payments-in-flow>"
  exit 1
fi

if [ -z "$TYPE" ]
then
  echo "No test type specified: sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <db-name> <subkey> <payments-in-flow>"
  exit 1
fi
if [ -z "$SCRIPT" ]
then
  echo "No script name specified: sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <db-name> <subkey> <payments-in-flow>"
  exit 1
fi
if [ -z "$DB_NAME" ]
then
  DB_NAME="fdr3_k6"
  echo "No DB name specified: 'fdr3_k6' is used."
fi
if [ -z "$PAYMENTS_IN_FLOW" ]
then
  PAYMENTS_IN_FLOW="1000"
  echo "No number of payments in flow specified: '1000' is used."
fi
if [ -z "$MAX_PARALLEL_CALLS" ]
then
  MAX_PARALLEL_CALLS="5"
  echo "No number of maximum parallel calls specified: '5' is used."
fi

export env=${ENVIRONMENT}
export type=${TYPE}
export script=${SCRIPT}
export db_name=${DB_NAME}
export sub_key=${API_SUBSCRIPTION_KEY}
export payments_in_flow=${PAYMENTS_IN_FLOW}
export max_parallel_calls=${MAX_PARALLEL_CALLS}

docker rm nginx
docker rm k6

stack_name=$(cd .. && basename "$PWD")
docker compose -p "${stack_name}-k6" up -d --remove-orphans --force-recreate --build
docker logs -f k6
docker stop nginx
