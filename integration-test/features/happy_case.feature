Feature: Happy case

  Background:
    Given systems up


  @runnable
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
          "totPayments": 3,
          "sumPayments": 0.03
        }
      """
    When PSP sends a create request to fdr-microservice with payload
    Then PSP receives the HTTP status code 201 to create request
