Feature: Check FdR published
#  Create a FdR
#  Add 3 payments
#  Publish FdR
#  Check if it is published
#  Check failed deletion of published FdR
#  Get FdR's details
#  Get FdR's payments details

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

  Scenario: Publish FdR
    Given the Add payments scenario executed successfully
    When PSP sends publish request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to publish request

  Scenario: Check FdR published
    Given the Publish FdR scenario executed successfully
    And the FdR revision is 1
    When PSP sends psp_get_published_fdr request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to psp_get_published_fdr request

  Scenario: Check published FdR payments
    Given the Check FdR published scenario executed successfully
    And the FdR revision is 1
    When PSP sends psp_get_published_payments request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to psp_get_published_payments request
    And PSP gets a list containing 3 counts of payments as a response of psp_get_published_payments request

  @runnable
  Scenario: Delete FdR
    Given the Check published FdR payments scenario executed successfully
    When PSP sends del_flow request to fdr-microservice with None
    Then PSP receives the HTTP status code 404 to del_flow request