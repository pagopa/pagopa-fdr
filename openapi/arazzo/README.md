# Runnable workflows

## Create new Flow

```
sh ./run_arazzo_workflow.sh \
--file_path ./fdr.arazzo.yaml \
--validate_arazzo true \
--log_level INFO \
--host api.uat.platform.pagopa.it/fdr-psp/service/v1 \
--api_key $UAT_FDR_PSP_APIKEY \
--workflow_id create-new-flow \
--input_file ./env/inputs.uat.env
```

## Create new revision for existing Flow

```
sh ./run_arazzo_workflow.sh \
--file_path ./fdr.arazzo.yaml \
--validate_arazzo false \
--log_level INFO \
--host api.uat.platform.pagopa.it/fdr-psp/service/v1 \
--api_key $UAT_FDR_PSP_APIKEY \
--workflow_id create-new-revision-for-existing-flow \
--input_file ./env/inputs.uat.env
```

## Failure during creating new Flow

```
sh ./run_arazzo_workflow.sh \
--file_path ./fdr.arazzo.yaml \
--validate_arazzo false \
--log_level INFO \
--host api.uat.platform.pagopa.it/fdr-psp/service/v1 \
--api_key $UAT_FDR_PSP_APIKEY \
--workflow_id failure-on-new-flow-creation \
--input_file ./env/inputs.uat.env
```

## Read published flow

```
sh ./run_arazzo_workflow.sh \
--file_path ./fdr.arazzo.yaml \
--validate_arazzo false \
--log_level INFO \
--host api.uat.platform.pagopa.it/fdr-org/service/v1 \
--api_key $UAT_FDR_ORG_APIKEY \
--workflow_id read-published-flow \
--input_file ./env/inputs.uat.env
```