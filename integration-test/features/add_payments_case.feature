Feature: Add multiple payments
#  Create a FdR
#  Add 1, 1000 and 1001 payments
#  Check responses are right

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


  @runnable
  Scenario Outline: Add payments
    Given PSP should sends <n> payments to the FdR
    And PSP should sends payments to the FdR whose sum is <amount>
    And the Create FdR scenario executed successfully
    When PSP adds <n> payments whose sum is <amount> to the FdR named flow_name like payload
    And PSP sends add_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code <code> to add_payments request
    Examples:
      | n                 | amount | code |
      | 1                 | 3      | 200  |
      | 1000              | 29999  | 200  |
      | 1001              | 30000  | 400  |
