import re
import os
import requests

def get_global_conf(context, field):
    return context.config.userdata.get("global_configuration").get(field)


def replace_local_variables(payload, context):
    pattern = re.compile('\\$\\w+\\$')
    match = pattern.findall(payload)
    for field in match:
        value = getattr(context, field.replace('$', '').split('.')[0])
        payload = payload.replace(field, value)
    return payload


def replace_global_variables(payload, context):
    pattern = re.compile('#\\w+#')
    match = pattern.findall(payload)
    for field in match:
        name = field.replace('#', '').split('.')[0]
        if name in context.config.userdata.get("global_configuration"):
            value = get_global_conf(context, name)
            payload = payload.replace(field, value)
    return payload


def get_fdr_url(request_type=""):
    request_type_mapping = {
        "create": {
            "endpoint": "/psps/#psp#/fdrs/$flow_name$",
            "method": "POST"
        }
    }
    return request_type_mapping.get(request_type)


def execute_request(url, method, headers, payload=None):
    return requests.request(method=method, url=url, headers=headers, data=payload, verify=False)



def get_subscription_key(context, config):
    data = context.config.userdata.get("services").get(config)
    if data.get("subscription_key") is not None:
        return os.getenv(data.get("subscription_key"))

    return None
