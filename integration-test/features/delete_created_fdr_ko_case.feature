Feature: Delete Created FdR
#  Create a FdR
#  Add 3 payments
#  Delete FdR
#  Check deletion of FdR

Background:
Given systems up


Scenario: Create FdR
Given an unique FdR name named flow_name
And an unique FdR date named flow_date
And an FdR flow like payload
  """
    {
      "fdr": "$flow_name$",
      "fdrDate": "$flow_date$",
      "sender": {
        "type": "LEGAL_PERSON",
        "id": "SELBIT2B",
        "pspId": "#psp#",
        "pspName": "Bank",
        "pspBrokerId": "#broker_psp#",
        "channelId": "#channel#",
        "password": "#channel_password#"
      },
      "receiver": {
        "id": "APPBIT2B",
        "organizationId": "#organization#",
        "organizationName": "Comune di XYZ"
      },
      "regulation": "SEPA - Bonifico xzy",
      "regulationDate": "$flow_date$",
      "bicCodePouringBank": "UNCRITMMXXX",
      "totPayments": $number_of_payments$,
      "sumPayments": $payments_amount$
    }
  """
When PSP sends create request to fdr-microservice with payload
Then PSP receives the HTTP status code 201 to create request

Scenario: Add payments
Given PSP should sends 3 payments to the FdR
And PSP should sends payments to the FdR whose sum is 300
And the Create FdR scenario executed successfully
When PSP adds 3 payments whose sum is 300 to the FdR named flow_name like payload
And PSP sends add_payments request to fdr-microservice with payload
Then PSP receives the HTTP status code 200 to add_payments request


Scenario:FdR deletion not found
Given the Add payments scenario executed successfully
And PSP sets an invalid flow_name
When PSP sends del_flow request to fdr-microservice with None
Then PSP receives the HTTP status code 404 to del_flow request

Scenario:FdR deletion bad request
  Given the Add payments scenario executed successfully
  And PSP sets an invalid psp
  When PSP sends del_flow request to fdr-microservice with None
  Then PSP receives the HTTP status code 400 to del_flow request

@runnable
Scenario:Unauthorized FdR deletion
  Given the FdR deletion not found scenario executed successfully
  When PSP with invalid subscription_key request del_flow to fdr-microservice with payload
  Then PSP receives the HTTP status code 401 to del_flow request

