Feature: Organization happy case
#  Get all published FdR

  Background:
    Given systems up

#  @runnable
  Scenario: Get all published FdR
    When Organization sends get_all_published request to fdr-microservice with None
    Then Organization receives the HTTP status code 200 to get_all_published request


#  @runnable
  Scenario: Get all published FdR
    And the psp configuration as pspId in query_params
    When Organization sends get_all_published request to fdr-microservice with None
    Then Organization receives the HTTP status code 200 to get_all_published request
    And Organization receives all FdR with the same pspId in the response of get_all_published request

  @runnable
  Scenario: Get all published FdR since yesterday
    And the psp configuration as pspId in query_params
    And Organization adds yesterday as publishedGt in query_params
    When Organization sends get_all_published request to fdr-microservice with None
    Then Organization receives the HTTP status code 200 to get_all_published request
    And Organization receives all FdR with pspId eq psp in the response of get_all_published request
    And Organization receives all FdR with published gt yesterday in the response of get_all_published request
