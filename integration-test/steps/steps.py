from behave import *
from datetime import timezone, timedelta
import logging
import requests
import datetime
import json
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
# x            headers = {'Host': 'api.dev.platform.pagopa.it:443'}
            resp = requests.get(url, headers=headers, verify=False)
            logging.debug(f"response: {resp.status_code}")
            responses &= (resp.status_code == 200)

        if responses:
            context.precondition_cache.add("systems up")

    assert responses


@given('an unique FdR {field_type} named {field_name}')
def step_impl(context, field_type, field_name):
    if field_type == 'name':
        today = datetime.datetime.today().astimezone(timezone.utc)
        fdr_name = today.strftime('%Y-%m-%d') + utils.get_global_conf(context, "psp") + "-" + today.strftime("%H%M%S%f")
        setattr(context, field_name, fdr_name)
    elif field_type == 'date':
        today = datetime.datetime.today().astimezone(timezone.utc)
        setattr(context, field_name, today.strftime("%Y-%m-%dT%H:%M:%S.%fZ"))


@given('PSP should sends {number_of} payments to the FdR')
def step_impl(context, number_of):
    setattr(context, "number_of_payments", number_of)


@given('PSP should sends payments to the FdR whose sum is {amount}')
def step_impl(context, amount):
    setattr(context, "payments_amount", amount)


@given('an FdR flow like {payload}')
def step_impl(context, payload):
    data = context.text or ""
    data = utils.replace_local_variables(data, context)
    data = utils.replace_global_variables(data, context)
    setattr(context, payload, data)


@when('{partner} sends {request_type} request to fdr-microservice with {payload}')
def step_impl(context, partner, request_type, payload):
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

    if hasattr(context, "query_params"):
        query_params = getattr(context, "query_params")
        delattr(context, "query_params")
        url += "?" + query_params

    data = None
    if payload != 'None':
        data = getattr(context, payload)
    response = utils.execute_request(url=url, method=endpoint_info.get("method"), headers=headers, payload=data)
    setattr(context, request_type + RESPONSE, response)


@then('{partner} receives the HTTP status code {http_status_code} to {request_type} request')
def step_impl(context, partner, http_status_code, request_type):
    response = getattr(context, request_type + RESPONSE)
    result = response.status_code == int(http_status_code)
    if not result:
        logging.info(f"status_code {response.status_code}, expected {http_status_code}, content {response.content}")
    assert result, f"status_code {response.status_code}, expected {http_status_code}"


@step('the {name} scenario executed successfully')
def step_impl(context, name):
    phase = (
            [phase for phase in context.feature.scenarios if name in phase.name] or [None])[0]
    text_step = ''.join(
        [step.keyword + " " + step.name + "\n\"\"\"\n" + (step.text or '') + "\n\"\"\"\n" for step in phase.steps])
    context.execute_steps(text_step)


@when('PSP adds {number} payments whose sum is {amount} to the FdR named {flow_name} like {payload}')
def step_impl(context, number, amount, flow_name, payload):
    payments = list()
    single_amount = float("{:.2f}".format(float(amount) / int(number)))
    today = datetime.datetime.today().astimezone(timezone.utc)
    for i in range(0, int(number)):
        pay_date = today - datetime.timedelta(days=i)
        single_payment = {
            "iuv": utils.generate_iuv(),
            "iur": utils.generate_iur(),
            "index": i+1,
            "pay": single_amount,
            "payStatus": "EXECUTED",
            "payDate": pay_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        }
        payments.append(single_payment)
    data = {
        "payments": payments
    }
    setattr(context, payload, json.dumps(data))


@when('PSP deletes {number_of} payments from the FdR named {flow_name} like {payload}')
def step_impl(context, number_of, flow_name, payload):
    data = {
        "indexList": list(range(1, int(number_of) + 1))
    }
    setattr(context, payload, json.dumps(data))


@then('PSP receives {number_of} payments in the response of {request_type} request')
def step_impl(context, number_of, request_type):
    response = getattr(context, request_type + RESPONSE)
    payload = json.loads(response.content)
    assert payload.get("count") == int(number_of)


@then('PSP receives {field_name} {field_value} in the response of {request_type} request')
def step_impl(context, field_name, field_value, request_type):
    response = getattr(context, request_type + RESPONSE)
    payload = json.loads(response.content)
    expected = payload.get(field_name)
    target = None
    try:
        target = int(field_value)
    except ValueError:
        target = field_value

    assert expected == target, f"field: {field_name} expected {expected}, target {target}"


@given('the FdR {revision} is {rev_number}')
def step_impl(context, revision, rev_number):
    setattr(context, revision, rev_number)


@then('PSP gets the FdR list containing {field_value} as {field_key} in the response of {request_type} request')
def step_impl(context, field_value, field_key, request_type):
    fdr_list = json.loads(getattr(context, request_type + RESPONSE).content)['data']
    result = False
    for fdr_element in fdr_list:
        if hasattr(context, field_value):
            field_value = getattr(context, field_value)

        if fdr_element[field_key] == field_value:
            result = True
            break
    assert result


# TODO remove
# @then('Organization receives all FdR with the same {field} in the response of {request_type} request')
# def step_impl(context, field, request_type):
#     response = getattr(context, request_type + RESPONSE)
#     payload = json.loads(response.content)
#     psp = utils.get_global_conf(context, "psp")
#     target = True
#     for item in payload.get("data"):
#         if field == "pspId" and item.get(field) != psp:
#             target = False
#     assert target


@then('Organization receives all FdR with {field_name} {operation} {field_value} '
      'in the response of {request_type} request')
def step_impl(context, field_name, operation, field_value, request_type):
    response = getattr(context, request_type + RESPONSE)
    payload = json.loads(response.content)
    global_field = utils.get_global_conf(context, field_value)
    if global_field is not None:
        field_value = global_field
    if hasattr(context, field_value):
        field_value = getattr(context, field_value)
    if field_value == "yesterday":
        field_value = utils.get_yesterday()
    target = True
    for item in payload.get("data"):
        if operation == "eq":
            if item[field_name] != field_value:
                target = False
                break
        if operation == "gt":
            if item[field_name] <= field_value:
                target = False
                break
    assert target


# @then('Organization receives all FdR with the published field {operation} {field_value} '
#       'in the response of {request_type} request')
# def step_impl(context, operation, field_value, request_type):
#     response = getattr(context, request_type + RESPONSE)
#     payload = json.loads(response.content)
#     target = True
#     for item in payload.get("data"):
#         if field == "pspId" and item.get(field) != psp:
#             target = False
#     assert target


@step('the {field} configuration as {field_key} in query_params')
def step_impl(context, field, field_key):
    value = utils.get_global_conf(context, field)
    utils.append_to_query_params(context, field_key + "=" + value)


@step('{partner} adds {field_value} as {field_key} in query_params')
def step_impl(context, partner, field_value, field_key):
    if field_value == "yesterday":
        field_value = utils.get_yesterday()
    elif hasattr(context, field_value):
        field_value = getattr(context, field_value)
    params = field_key + "=" + field_value
    utils.append_to_query_params(context, params)

# @step('{test}')
# def step_impl(context, test):
#     print("TEST")
#     pass
