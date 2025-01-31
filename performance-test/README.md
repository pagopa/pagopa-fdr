# K6 tests

This is a set of [k6](https://k6.io) tests.

To invoke k6 tests use `run_performance_test.sh` script.

## How to run 🚀

Use this command to launch the tests:

``` shell
sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-filename> <db-name> <subkey> <payments-in-flow>
```

sh run_performance_test.sh dev smoke create_flow_sequential k6 b139f9e7c4bb47ae8173d8a09da92313 10