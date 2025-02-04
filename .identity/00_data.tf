data "azurerm_storage_account" "tf_storage_account" {
  name                = "pagopainfraterraform${var.env}"
  resource_group_name = "io-infra-rg"
}

data "azurerm_resource_group" "dashboards" {
  name = "dashboards"
}

data "azurerm_resource_group" "apim_resource_group" {
  name = "${local.product}-api-rg"
}

data "azurerm_kubernetes_cluster" "aks" {
  name                = local.aks_cluster.name
  resource_group_name = local.aks_cluster.resource_group_name
}

data "github_organization_teams" "all" {
  root_teams_only = true
  summary_only    = true
}

data "azurerm_key_vault" "key_vault" {
  name                = "pagopa-${var.env_short}-kv"
  resource_group_name = "pagopa-${var.env_short}-sec-rg"
}

data "azurerm_key_vault" "domain_key_vault" {
  name                = "pagopa-${var.env_short}-${local.domain}-kv"
  resource_group_name = "pagopa-${var.env_short}-${local.domain}-sec-rg"
}

data "azurerm_key_vault" "nodo_key_vault" {
  name                = "pagopa-${var.env_short}-nodo-kv"
  resource_group_name = "pagopa-${var.env_short}-nodo-sec-rg"
}

data "azurerm_key_vault_secret" "key_vault_sonar" {
  name         = "sonar-token"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_bot_cd_token" {
  name         = "pagopa-platform-domain-github-bot-cd-pat"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_cucumber_token" {
  name         = "cucumber-token"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "integration_test_internal_subscription_key" {
  count        = var.env_short == "p" ? 0 : 1
  name         = "integration-test-internal-subscription-key"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "integration_test_psp_subscription_key" {
  count        = var.env_short == "p" ? 0 : 1
  name         = "integration-test-psp-subscription-key"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "integration_test_org_subscription_key" {
  count        = var.env_short == "p" ? 0 : 1
  name         = "integration-test-org-subscription-key"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "opex_internal_subscription_key" {
  count        = var.env_short == "p" ? 1 : 0
  name         = "opex-internal-subscription-key"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "opex_psp_subscription_key" {
  count        = var.env_short == "p" ? 1 : 0
  name         = "opex-psp-subscription-key"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "opex_org_subscription_key" {
  count        = var.env_short == "p" ? 1 : 0
  name         = "opex-org-subscription-key"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_deploy_slack_webhook" {
  name         = "pagopa-pagamenti-deploy-slack-webhook"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_integration_test_slack_webhook" {
  name         = "pagopa-pagamenti-integration-test-slack-webhook"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

#data "azurerm_resource_group" "app_rg" {
#  name  = "${local.prefix}-${var.env_short}-${local.location_short}-${local.domain}-rg"
#}
#
#data "azurerm_storage_account" "integration_test_storage_account" {
#  name                = local.integration_test.storage_account_name
#  resource_group_name = local.integration_test.storage_account_rg
#}

data "azurerm_user_assigned_identity" "identity_cd" {
  name                = "${local.product}-${local.domain}-01-github-cd-identity"
  resource_group_name = "${local.product}-identity-rg"
}

data "azurerm_user_assigned_identity" "identity_ci" {
  count  = var.env_short == "p" ? 0 : 1
  
  name                = "${local.product}-${local.domain}-01-github-ci-identity"
  resource_group_name = "${local.product}-identity-rg"
}