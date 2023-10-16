import re
import os
import requests
import logging
import contextlib
from http.client import HTTPConnection


def debug_requests_on():
    '''Switches on logging of the requests module.'''
    HTTPConnection.debuglevel = 1

    logging.basicConfig()
    logging.getLogger().setLevel(logging.DEBUG)
    requests_log = logging.getLogger("requests.packages.urllib3")
    requests_log.setLevel(logging.DEBUG)
    requests_log.propagate = True


def debug_requests_off():
    '''Switches off logging of the requests module, might be some side-effects'''
    HTTPConnection.debuglevel = 0

    root_logger = logging.getLogger()
    root_logger.setLevel(logging.WARNING)
    root_logger.handlers = []
    requests_log = logging.getLogger("requests.packages.urllib3")
    requests_log.setLevel(logging.WARNING)
    requests_log.propagate = False


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


# @contextlib.contextmanager
def execute_request(url, method, headers, payload=None):
    debug_requests_on()
    req = requests.request(method=method, url=url, headers=headers, data=payload)
    debug_requests_off()
    return req


def get_subscription_key(context, config):
    data = context.config.userdata.get("services").get(config)
    if data.get("subscription_key") is not None:
        return os.getenv(data.get("subscription_key"))

    return None
