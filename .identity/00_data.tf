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

data "azurerm_key_vault_secret" "key_vault_sonar" {
  name         = "sonar-token"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_bot_token" {
  name         = "bot-token-github"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_cucumber_token" {
  name         = "cucumber-token"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_slack_webhook_url" {
  name         = "slack-webhook-url"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
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

data "azurerm_user_assigned_identity" "identity_cd" {
  name                = "${local.product}-${local.domain}-01-github-cd-identity"
  resource_group_name = "${local.product}-identity-rg"
}
