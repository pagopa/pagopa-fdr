Feature: Verify revision workflow
#  Create a FdR
#  Add 3 payments
#  Publish FdR
#  Check revision
#  Create FdR rev 2 with 2 payments
#  Check revision
#  Add 2 payments
#  Publish FdR
#  Check revision
#  Create FdR rev 3 with 1 payments
#  Check revision
#  Add 2 payments
#  Check Publish FdR fails

  Background:
    Given systems up


  Scenario: Create FdR
    Given an unique FdR name named flow_name
    And an unique FdR date named flow_date
    And an FdR flow like create_1_payload
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
    When PSP sends create request to fdr-microservice with create_1_payload
    Then PSP receives the HTTP status code 201 to create request


  Scenario: Add 3 payments
    Given PSP should sends 3 payments to the FdR
    And PSP should sends payments to the FdR whose sum is 300
    And the Create FdR scenario executed successfully
    When PSP adds 3 payments whose sum is 300 to the FdR named flow_name like payload
    And PSP sends add_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code 200 to add_payments request


  Scenario: Publish FdR
    Given the Add 3 payments scenario executed successfully
    When PSP sends publish request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to publish request


  Scenario: Check revision after first publish
    Given the Publish FdR scenario executed successfully
    And the FdR revision is 1
    When PSP sends psp_get_published_fdr request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to psp_get_published_fdr request
    And PSP receives revision 1 in the response of psp_get_published_fdr request

  Scenario: Create the same FdR
    Given an FdR flow like create_2_payload
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
    When PSP sends create request to fdr-microservice with create_2_payload
    Then PSP receives the HTTP status code 201 to create request


  Scenario: Create revision 2
    Given the Check revision after first publish scenario executed successfully
    And PSP should sends 2 payments to the FdR
    And the Create the same FdR scenario executed successfully
    When PSP sends created_fdr request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to created_fdr request
    And PSP receives revision 2 in the response of created_fdr request
    And PSP receives status CREATED in the response of created_fdr request


  Scenario: Add 2 payments
    Given the Create revision 2 scenario executed successfully
    When PSP adds 2 payments whose sum is 300 to the FdR named flow_name like payload
    And PSP sends add_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code 200 to add_payments request


  Scenario: Publish FdR revision 2
    Given the Add 2 payments scenario executed successfully
    When PSP sends publish request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to publish request


  Scenario: Check revision after second publish
    Given the Publish FdR revision 2 scenario executed successfully
    And the FdR revision is 2
    When PSP sends psp_get_published_fdr request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to psp_get_published_fdr request
    And PSP receives revision 2 in the response of psp_get_published_fdr request
    And PSP receives totPayments 2 in the response of psp_get_published_fdr request


  Scenario: Create revision 3
    Given the Check revision after second publish scenario executed successfully
    And PSP should sends 1 payments to the FdR
    And the Create the same FdR scenario executed successfully
    When PSP sends created_fdr request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to created_fdr request
    And PSP receives revision 3 in the response of created_fdr request
    And PSP receives status CREATED in the response of created_fdr request


  Scenario: Add 2 payments instead of 1
    Given the Create revision 3 scenario executed successfully
    When PSP adds 2 payments whose sum is 300 to the FdR named flow_name like payload
    And PSP sends add_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code 200 to add_payments request


  @runnable
  Scenario: Try to publish FdR revision 3
    Given the Add 2 payments instead of 1 scenario executed successfully
    When PSP sends publish request to fdr-microservice with None
    Then PSP receives the HTTP status code 400 to publish request
