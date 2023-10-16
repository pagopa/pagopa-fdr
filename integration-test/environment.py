import json
from behave.model import Table
import os

import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


def before_all(context):
    # initialize precondition cache to avoid check systems up for each scenario
    context.precondition_cache = set()

    context.config.setup_logging()

    print('Global settings...')

    more_userdata = json.load(open(os.path.join(context.config.base_dir + "/config/config.json")))
    context.config.update_userdata(more_userdata)


def before_feature(context, feature):
    services = context.config.userdata.get("services")
    # add heading
    feature.background.steps[0].table = Table(headings=("name", "url", "healthcheck", "subscription_key"))
    # add data in the tables
    for system_name in services.keys():
        row = (
            system_name,
            services.get(system_name).get("url", ""),
            services.get(system_name).get("healthcheck", ""),
            os.environ.get(services.get(system_name).get("subscription_key", ""))
        )
        feature.background.steps[0].table.add_row(row)


def after_feature(context, feature):
    global_configuration = context.config.userdata.get("global_configuration")


def after_all(context):
    print("After all disabled")
    # db_selected = context.config.userdata.get("db_configuration").get('nodo_cfg')
    # conn = db.getConnection(db_selected.get('host'), db_selected.get('database'), db_selected.get('user'), db_selected.get('password'),db_selected.get('port'))
    #
    # config_dict = getattr(context, 'configurations')
    # for key, value in config_dict.items():
    #     #print(key, value)
    #     selected_query = utils.query_json(context, 'update_config', 'configurations').replace('value', value).replace('key', key)
    #     db.executeQuery(conn, selected_query)
    #
    # db.closeConnection(conn)
    # headers = {'Host': 'api.dev.platform.pagopa.it:443'}
    # requests.get(utils.get_refresh_config_url(context), verify=False, headers=headers)
