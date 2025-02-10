locals {
  product = "${var.prefix}-${var.env_short}"
  project = "${var.prefix}-${var.env_short}-${var.location_short}-${var.domain}"

  apim = {
    name           = "${local.product}-apim"
    rg             = "${local.product}-api-rg"
    psp_product_id = "fdr-psp"
    org_product_id = "fdr-org"
    int_product_id = "fdr_internal"
  }

  apim_hostname = "api.${var.apim_dns_zone_prefix}.${var.external_domain}"
  hostname      = var.env == "prod" ? "weuprod.fdr.internal.platform.pagopa.it" : "weu${var.env}.fdr.internal.${var.env}.platform.pagopa.it"
}
