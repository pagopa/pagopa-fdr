Feature: Delete payments
#  Create a FdR
#  Add 3 payments
#  Check FdR contains 3 payments
#  Remove 1 payments
#  Check FdR contains 2 payments
#  Check publish FdR fails

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

  Scenario: Created payments after add
    Given the Add payments scenario executed successfully
    When PSP sends created_payments request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to created_payments request
    And PSP receives 3 payments in the response of created_payments request

  Scenario: Delete payments
    Given the Created payments scenario executed successfully
    When PSP deletes 1 payments from the FdR named flow_name like payload
    And PSP sends del_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code 200 to del_payments request

  Scenario: Created payments after delete
    Given the Delete payments scenario executed successfully
    When PSP sends created_payments request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to created_payments request
    And PSP receives 2 payments in the response of created_payments request

  @runnable
  Scenario: Publish FdR
    Given the Created payments after delete scenario executed successfully
    When PSP sends publish request to fdr-microservice with None
    Then PSP receives the HTTP status code 400 to publish request
