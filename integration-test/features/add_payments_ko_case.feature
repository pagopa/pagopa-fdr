Feature: Verify ko of payments
#  Create a FdR
#  Add 1001 payments to check 400 status code
#  Add payments with an invalid subscription_key to check 401 status code


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

  Scenario: Add too many payments
    Given PSP should sends 1001 payments to the FdR
    And PSP should sends payments to the FdR whose sum is 9999
    And the Create FdR scenario executed successfully
    When PSP adds 1001 payments whose sum is 9999 to the FdR named flow_name like payload
    And PSP sends add_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code 400 to add_payments request

  @runnable
  Scenario: Add payments invalid subscription_key
    Given PSP should sends 3 payments to the FdR
    And PSP should sends payments to the FdR whose sum is 10
    And the Add too many payments scenario executed successfully
    When PSP adds 3 payments whose sum is 10 to the FdR named flow_name like payload
    And PSP with invalid subscription_key request add_payments to fdr-microservice with payload
    Then PSP receives the HTTP status code 401 to add_payments request