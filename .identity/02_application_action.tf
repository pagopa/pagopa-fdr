resource "azurerm_storage_container" "test-data-container" {
  count                 = var.env_short == "p" ? 0 : 1
  name                  = local.github.repository
  storage_account_name  = data.azurerm_storage_account.integration_test_storage_account.name
  container_access_type = "blob"
}
