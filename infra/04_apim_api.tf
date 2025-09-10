###############
##  API FdR  ##
###############
locals {
  apim_fdr_psp_service_api = {
    display_name          = "FDR Fase3 - Flussi di rendicontazione (PSP)"
    description           = "FDR - Flussi di rendicontazione (PSP)"
    path                  = "fdr-psp/service"
    subscription_required = true
    service_url           = null
  }

  apim_fdr_org_service_api = {
    display_name          = "FDR Fase3 - Flussi di rendicontazione (ORGS)"
    description           = "FDR - Flussi di rendicontazione (ORGS)"
    path                  = "fdr-org/service"
    subscription_required = true
    service_url           = null
  }

  apim_fdr_service_api_internal = {
    display_name          = "FDR Fase3 - Flussi di rendicontazione (INTERNAL)"
    description           = "FDR - Flussi di rendicontazione (INTERNAL)"
    path                  = "fdr-internal/service"
    subscription_required = true
    service_url           = null
  }
}

##################
##  API FdR PSP ##
##################

resource "azurerm_api_management_api_version_set" "api_fdr_api_psp" {
  name                = "${var.env_short}-fdr-service-api-psp"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_fdr_psp_service_api.display_name
  versioning_scheme   = "Segment"
}


module "apim_api_fdr_api_v1_psp" {
  source = "./.terraform/modules/__v3__/api_management_api"

  name                  = "${local.project}-fdr-service-api-psp"
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.psp_product_id]
  subscription_required = local.apim_fdr_psp_service_api.subscription_required
  version_set_id        = azurerm_api_management_api_version_set.api_fdr_api_psp.id
  api_version           = "v1"

  description  = local.apim_fdr_psp_service_api.description
  display_name = local.apim_fdr_psp_service_api.display_name
  path         = local.apim_fdr_psp_service_api.path
  protocols    = ["https"]
  service_url  = local.apim_fdr_psp_service_api.service_url

  content_format = "openapi"

  content_value = templatefile("./api/psp/openapi.json", {
    host = local.apim_hostname
  })

  xml_content = templatefile("./policy/psp/v1/_base_policy.xml.tpl", {
    hostname = local.hostname
  })
}

##################
##  API FdR ORG ##
##################

resource "azurerm_api_management_api_version_set" "api_fdr_api_org" {
  name                = "${var.env_short}-fdr-service-api-org"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_fdr_org_service_api.display_name
  versioning_scheme   = "Segment"
}


module "apim_api_fdr_api_v1_org" {
  source = "./.terraform/modules/__v3__/api_management_api"

  name                  = "${local.project}-fdr-service-api-org"
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.org_product_id]
  subscription_required = local.apim_fdr_org_service_api.subscription_required
  version_set_id        = azurerm_api_management_api_version_set.api_fdr_api_org.id
  api_version           = "v1"

  description  = local.apim_fdr_org_service_api.description
  display_name = local.apim_fdr_org_service_api.display_name
  path         = local.apim_fdr_org_service_api.path
  protocols    = ["https"]
  service_url  = local.apim_fdr_org_service_api.service_url

  content_format = "openapi"

  content_value = templatefile("./api/org/openapi.json", {
    host = local.apim_hostname
  })

  xml_content = templatefile("./policy/org/v1/_base_policy.xml.tpl", {
    hostname = local.hostname
  })
}

#######################
##  API FdR INTERNAL ##
#######################
resource "azurerm_api_management_api_version_set" "api_fdr_api_internal" {
  name                = "${var.env_short}-fdr-service-api-internal"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  display_name        = local.apim_fdr_service_api_internal.display_name
  versioning_scheme   = "Segment"
}

module "apim_api_fdr_api_v1_internal" {
  source = "./.terraform/modules/__v3__/api_management_api"

  name                  = "${local.project}-fdr-service-api-internal"
  api_management_name   = local.apim.name
  resource_group_name   = local.apim.rg
  product_ids           = [local.apim.int_product_id]
  subscription_required = local.apim_fdr_service_api_internal.subscription_required
  version_set_id        = azurerm_api_management_api_version_set.api_fdr_api_internal.id
  api_version           = "v1"

  description  = local.apim_fdr_service_api_internal.description
  display_name = local.apim_fdr_service_api_internal.display_name
  path         = local.apim_fdr_service_api_internal.path
  protocols    = ["https"]
  service_url  = local.apim_fdr_service_api_internal.service_url

  content_format = "openapi"
  content_value  = templatefile("./api/internal/openapi.json", {
    host = local.apim_hostname
  })

  xml_content = templatefile("./policy/_base_policy.xml.tpl", {
    hostname = local.hostname
  })
}

#####################################
##  Policies for specific APIs     ##
#####################################

resource "azurerm_api_management_api_operation_policy" "fdr3_add_payments_rate_limit" {
  api_name            = "${local.project}-fdr-service-api-psp-v1"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  operation_id        = "IPspsController_addPaymentToExistingFlow"
  xml_content = templatefile("./policy/psp/v1/_base_policy_payments_add.xml.tpl", {
    hostname = local.hostname
  })
}

resource "azurerm_api_management_api_operation_policy" "fdr3_delete_payments_rate_limit" {
  api_name            = "${local.project}-fdr-service-api-psp-v1"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  operation_id        = "IPspsController_deletePaymentFromExistingFlow"
  xml_content = templatefile("./policy/psp/v1/_base_policy_payments_delete.xml.tpl", {
    hostname = local.hostname
  })
}

resource "azurerm_api_management_api_operation_policy" "fdr3_get_single_flow" {
  api_name            = "${local.project}-fdr-service-api-org-v1"
  resource_group_name = local.apim.rg
  api_management_name = local.apim.name
  operation_id        = "IOrganizationsController_getSinglePublishedFlow"
  xml_content = templatefile("./policy/org/v1/_base_policy_get_single_flow.xml.tpl", {
    hostname = local.hostname
  })
}

#######################
##  Policies SHA     ##
#######################

# https://github.com/hashicorp/terraform-provider-azurerm/issues/17016#issuecomment-1314991599
# https://learn.microsoft.com/en-us/azure/templates/microsoft.apimanagement/2022-04-01-preview/service/policyfragments?pivots=deployment-language-terraform

resource "terraform_data" "sha256_fdr3_policy_psps_v1" {
  input = sha256(templatefile("./policy/psp/v1/_base_policy.xml.tpl", {
    hostname = local.hostname
  }))
}

resource "terraform_data" "sha256_fdr3_policy_orgs_v1" {
  input = sha256(templatefile("./policy/org/v1/_base_policy.xml.tpl", {
    hostname = local.hostname
  }))
}

resource "terraform_data" "sha256_fdr3_policy_base" {
  input = sha256(templatefile("./policy/_base_policy.xml.tpl", {
    hostname = local.hostname
  }))
}

resource "terraform_data" "sha256_fdr3_add_payments_policy_base" {
  input = sha256(templatefile("./policy/psp/v1/_base_policy_payments_add.xml.tpl", {
    hostname = local.hostname
  }))
}

resource "terraform_data" "sha256_fdr3_delete_payments_policy_base" {
  input = sha256(templatefile("./policy/psp/v1/_base_policy_payments_delete.xml.tpl", {
    hostname = local.hostname
  }))
}

resource "terraform_data" "sha256_fdr3_org_get_single_flow_policy_base" {
  input = sha256(templatefile("./policy/org/v1/_base_policy_get_single_flow.xml.tpl", {
    hostname = local.hostname
  }))
}