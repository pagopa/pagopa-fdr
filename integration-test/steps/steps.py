from behave import *
import logging
import requests
import datetime

import utils as utils

# Constants
RESPONSE = "RES"
REQUEST = "REQ"

@given('systems up')
def step_impl(context):
    """
        health check for defined systems
    """
    responses = True

    if "systems up" not in context.precondition_cache:

        for row in context.table:
            logging.debug(f"calling: {row.get('name')} -> {row.get('url')}")

            url = row.get("url") + row.get("healthcheck")
            logging.debug(f"calling -> {url}")
            subscription_key = utils.get_subscription_key(context, "fdr")
            headers = {'Content-Type': 'application/json'}
            if subscription_key is not None:
                headers['Ocp-Apim-Subscription-Key'] = subscription_key
#             headers = {'Host': 'api.dev.platform.pagopa.it:443'}
            resp = requests.get(url, headers=headers, verify=False)
            logging.debug(f"response: {resp.status_code}")
            responses &= (resp.status_code == 200)

        if responses:
            context.precondition_cache.add("systems up")

    assert responses


@given('an unique FdR {field_type} named {field_name}')
def step_impl(context, field_type, field_name):
    if field_type == 'name':
        today = datetime.datetime.today()
        fdr_name = today.strftime('%Y-%m-%d') + utils.get_global_conf(context, "psp") + "-" + today.strftime("%H%M%S%f")
        setattr(context, field_name, fdr_name)
    elif field_type == 'date':
        today = datetime.datetime.today()
        setattr(context, field_name, today.strftime('%Y-%m-%dT%H:%M:%SZ'))


@given('an FdR flow like {payload}')
def step_impl(context, payload):
    data = context.text or ""
    data = utils.replace_local_variables(data, context)
    data = utils.replace_global_variables(data, context)
    setattr(context, payload, data)


@when('PSP sends a {request_type} request to fdr-microservice with {payload}')
def step_impl(context, request_type, payload):

    subscription_key = utils.get_subscription_key(context, "fdr")
    headers = {'Content-Type': 'application/json'}
    if subscription_key is not None:
        headers['Ocp-Apim-Subscription-Key'] = subscription_key

    fdr_config = context.config.userdata.get("services").get("fdr")
    endpoint_info = utils.get_fdr_url(request_type)
    endpoint = utils.replace_local_variables(endpoint_info.get("endpoint"), context)
    endpoint = utils.replace_global_variables(endpoint, context)
    endpoint_info["endpoint"] = endpoint
    url = fdr_config.get("url") + endpoint
    response = utils.execute_request(url=url, method=endpoint_info.get("method"), headers=headers, payload=getattr(context, payload))
    setattr(context, request_type + RESPONSE, response)


@then('PSP receives the HTTP status code {http_status_code} to {request_type} request')
def step_impl(context, http_status_code, request_type):
    response = getattr(context, request_type + RESPONSE)
    assert (response.status_code == int(http_status_code)), f"status_code {response.status_code}, expected {http_status_code}"

@given('{test}')
@when('{test}')
@then('{test}')
def step_impl(context):
    print("TEST")
    pass
