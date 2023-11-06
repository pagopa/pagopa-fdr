Feature: Check pageable PSP
#  Create a FdR
#  Add 100 payments
#  Receive first page of payments
#  Receive second page of payments
#  Receive first page of FdR
#  Receive second page of FdRs

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
    Given PSP should sends 100 payments to the FdR
    And PSP should sends payments to the FdR whose sum is 20
    And the Create FdR scenario executed successfully
    When PSP adds 100 payments whose sum is 20 to the FdR named flow_name like payload
    And PSP sends add_payments request to fdr-microservice with payload
    Then PSP receives the HTTP status code 200 to add_payments request

  Scenario: Publish FdR
    Given the Add payments scenario executed successfully
    When PSP sends publish request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to publish request

  @runnable
  Scenario: Check created payments pagination 1
    Given the Add payments scenario executed successfully
    And PSP adds flow_date as createdGt in query_params
    And PSP adds 1 as page in query_params
    And PSP adds 99 as size in query_params
    When PSP sends created_payments request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to created_payments request
    And PSP receives page 1 with 99 entries as response of created_payments request

  @runnable
  Scenario: Check created payments pagination 2
    Given the Add payments scenario executed successfully
    And PSP adds flow_date as createdGt in query_params
    And PSP adds 2 as page in query_params
    And PSP adds 1 as size in query_params
    When PSP sends created_payments request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to created_payments request
    And PSP receives page 2 with 1 entries as response of created_payments request

  @runnable
  Scenario: Check created FdR pagination 1
    Given the Add payments scenario executed successfully
    And PSP adds yesterday as createdGt in query_params
    And PSP adds 1 as page in query_params
    And PSP adds 1 as size in query_params
    When PSP sends get_all_created request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to get_all_created request
    And PSP receives page 1 with 1 entries as response of get_all_created request

  @runnable
  Scenario: Check created FdR pagination 2
    Given the Add payments scenario executed successfully
    And PSP adds yesterday as createdGt in query_params
    And PSP adds 2 as page in query_params
    And PSP adds 1 as size in query_params
    When PSP sends get_all_created request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to get_all_created request
    And PSP receives page 2 with 1 entries as response of get_all_created request

  @runnable
  Scenario Outline: Check published FdR payments pagination
    Given the Publish FdR scenario executed successfully
    And the FdR revision is 1
    And PSP adds yesterday as createdGt in query_params
    And PSP adds <page_number> as page in query_params
    And PSP adds <page_size> as size in query_params
    When PSP sends psp_get_published_payments request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to psp_get_published_payments request
    And PSP receives page <page_number> with <page_size> entries as response of psp_get_published_payments request
    Examples:
      | page_number  | page_size |
      | 1            | 1         |
      | 2            | 50        |

  @runnable
  Scenario Outline: Check published FdRs pagination
    Given the Publish FdR scenario executed successfully
    And the FdR revision is 1
    And the psp configuration as pspId in query_params
    And the organization configuration as organizationId in query_params
    And PSP adds yesterday as publishedGt in query_params
    And PSP adds <page_number> as page in query_params
    And PSP adds <page_size> as size in query_params
    When PSP sends psp_get_all_published_fdr request to fdr-microservice with None
    Then PSP receives the HTTP status code 200 to psp_get_all_published_fdr request
    And PSP receives page <page_number> with <page_size> entries as response of psp_get_all_published_fdr request
    Examples:
      | page_number  | page_size |
      | 1            | 1         |
      | 2            | 2         |
