import json, logging, requests, time
import utility


def create_empty_flow(url, flow_name, flow_date, total_payments, key):

    headers = {
        "Ocp-Apim-Subscription-Key": key
    }
    request = {
        "fdr": flow_name,
        "fdrDate": f"{flow_date}T12:00:00.000Z",
        "sender": {
            "type": "LEGAL_PERSON",
            "id": "SELBIT2B",
            "pspId": "88888888888",
            "pspName": "Bank",
            "pspBrokerId": "88888888888",
            "channelId": "88888888888_01",
            "password": "PLACEHOLDER"
        },
        "receiver": {
            "id": "APPBIT2B",
            "organizationId": "15376371009",
            "organizationName": "PagoPA"
        },
        "regulation": "SEPA - Bonifico xzy",
        "regulationDate": f"{flow_date}T12:00:00.000Z",
        "bicCodePouringBank": "UNCRITMMXXX",
        "totPayments": total_payments,
        "sumPayments": total_payments * 10
    }
    logging.info(f"\nSend request to: [{url}]\nRequest: [{request}]")
    start_time = time.time()
    response = requests.post(url=url,
                             data=json.dumps(request),
                             headers=headers)
    end_time = time.time()
    logging.info(f"=====\nElapsed time: [{(end_time - start_time):.3f} sec] Status Code: [{response.status_code}]\nResponse: {response.json()}\n==================")



def add_payments(url, total_payments, max_per_add, date, key):

    headers = {
        "Ocp-Apim-Subscription-Key": key
    }

    total_amount = total_payments * 10
    payments = generate_payments(total_payments, total_amount, date)
    request = []

    total_requests = int(total_payments / max_per_add)
    for req_idx in range(total_requests):
        extracted_payments = {
            "payments": payments[(req_idx * max_per_add):(req_idx * max_per_add + max_per_add)]
        }
        request = json.dumps(extracted_payments)
        logging.info(f"\nSend request to: [{url}]\nRequest: [TOO LONG]")
        start_time = time.time()
        response = requests.put(url=url,
                                data=request,
                                headers=headers)
        end_time = time.time()
        logging.info(f"=====\nElapsed time: [{(end_time - start_time):.3f} sec] Status Code: [{response.status_code}]\nResponse: {response.json()}\n==================")


def publish_payments(url, key):
    headers = {
        "Ocp-Apim-Subscription-Key": key
    }
    logging.info(f"\nSend request to: [{url}]")
    start_time = time.time()
    response = requests.post(url=url,
                             headers=headers)
    end_time = time.time()
    logging.info(f"=====\nElapsed time: [{(end_time - start_time):.3f} sec] Status Code: [{response.status_code}]\nResponse: {response.json()}\n==================")


#########
# UTILS #
#########
def generate_payments(number_of_payments, total_amount, date):

    payments = []
    iuvs = set()
    for idx in range(number_of_payments):
        iuv = utility.get_random_numeric_string(15)
        if iuv not in iuvs:
            iuvs.add(iuv)
            payments.append({
                "index": idx + 1,
                "iuv": iuv,
                "iur": utility.get_random_numeric_string(11),
                "pay": total_amount / number_of_payments,
                "idTransfer": 1,
                "payStatus": "EXECUTED",
                "payDate": f"{date}T12:00:00.000Z"
            })
        else:
            idx -= 1
    return payments

def get_random_numeric_string(size, dataset = "0123456789"):
    random_string = ''.join(random.choices(dataset, k=size))
    return random_string