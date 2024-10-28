import sys
import logging, time
import methods


NUMBER_OF_PAYMENTS = 5000
MAX_PAYMENTS_PER_ADD_OPERATION = 1000

def main(URL, subkey):
    logging.basicConfig(level=logging.INFO)

    flow_date = "2024-10-15"
    tmstmp = timestamp = int(time.time())
    flow_name = f"{flow_date}88888888888-{tmstmp}"

    create_url = URL + f"/psps/88888888888/fdrs/{flow_name}"
    methods.create_empty_flow(create_url, flow_name, flow_date, NUMBER_OF_PAYMENTS, subkey)


    add_url = URL + f"/psps/88888888888/fdrs/{flow_name}/payments/add"
    methods.add_payments(add_url, NUMBER_OF_PAYMENTS, MAX_PAYMENTS_PER_ADD_OPERATION, flow_date, subkey)


    publish_url = URL + f"/psps/88888888888/fdrs/{flow_name}/publish"
    methods.publish_payments(publish_url, subkey)

def get_url(env):
    if env == 'dev':
        return "https://api.dev.platform.pagopa.it/fdr-psp/service/v1"
    elif env == 'uat':
        return "https://upload.uat.platform.pagopa.it/fdr-psp/service/v1"
    else:
        raise ValueError(f"Invalid environment: {env}. Please use 'dev' or 'uat'.")

if __name__ == "__main__":
    try:
        env = sys.argv[1]
        key = sys.argv[2]
        url = get_url(env)
        main(url, key)
    except IndexError:
        print("Usage: python3 main.py <environment> <sukey> \ni.e. python3 main.py dev your-key")
    except ValueError as e:
        print(e)