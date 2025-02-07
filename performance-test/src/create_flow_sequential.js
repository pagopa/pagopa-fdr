import http from "k6/http";
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { generateFlowNameAndDate, generatePartitionIndexes } from './modules/helper.js';
import { buildCreateFlowRequest, buildAddPaymentsRequest } from './modules/request_builder.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
const varsArray = new SharedArray("vars", function () {
  return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
const vars = varsArray[0];


// initialize parameters taken from env
const fdrBaseUrl = `${vars.base_path}`;
const requestValues = {
  pspType: `${vars.psp_type}`,
  pspId: `${vars.psp_id}`,
  pspDomainId: `${vars.psp_domain_id}`,
  pspName: `${vars.psp_name}`,
  pspBrokerId: `${vars.psp_broker_id}`,
  channelId: `${vars.channel_id}`,
  creditorInstitutionId: `${vars.creditor_institution_id}`,
  creditorInstitutionDomainId: `${vars.creditor_institution_domain_id}`,
  creditorInstitutionName: `${vars.creditor_institution_name}`
}

const paymentsInFlow = `${__ENV.PAYMENTS_IN_FLOW}`;
const totalAmount = paymentsInFlow * 100.00;
const numberOfPartitions = 1 + (paymentsInFlow / 1000);

const subscriptionKey = `${__ENV.API_SUBSCRIPTION_KEY}`;


var params = {};

export function setup() {
  // Before All
  params = {
    tags: { 'api_name': '' },
    headers: {
      'Content-Type': 'application/json',
      'Ocp-Apim-Subscription-Key': subscriptionKey
    },
  };
}

export default function () {

  setup();

  // Generating essentials information
  //console.log(`VU: ${__VU}  -  ITER: ${__ITER}`);
  let flowNameAndDate = generateFlowNameAndDate(requestValues.pspDomainId, `${__VU}`);
  let flowName = flowNameAndDate[0]
  let flowDate = flowNameAndDate[1]

  // Create a new flow
  let createFlowRequest = buildCreateFlowRequest(flowName, flowDate, paymentsInFlow, totalAmount, requestValues);
  const createFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}`
  params.tags.api_name = "create_empty_flow";
  var createFlowResponse = http.post(createFlowUrl, createFlowRequest, params);
  //console.log(`\nCreate flowrequest: ${createFlowRequest}  -  response: ${createFlowResponse.status} - ${createFlowResponse.body}`);
  check(createFlowResponse, {
    'Check if empty flow was created [HTTP Code: 201]': (_r) => createFlowResponse.status === 201,
  });
  if (createFlowResponse.status !== 201) {
    console.log(`Create flow in error: ${createFlowUrl} => response: ${createFlowResponse.status} - ${createFlowResponse.body}`);
    return;
  }

  const partitions = generatePartitionIndexes(paymentsInFlow, 1000);
  const addPaymentsUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/payments/add`
  params.tags.api_name = "add_payment";
  for (const partition of partitions) {

    // Add new payments
    let addPaymentsRequest = buildAddPaymentsRequest(partition, 100.00, flowDate);
    var addPaymentsResponse = http.put(addPaymentsUrl, addPaymentsRequest, params);
    check(addPaymentsResponse, {
      'Check if payments were added to flow [HTTP Code: 200]': (_r) => addPaymentsResponse.status === 200,
    });
    if (addPaymentsResponse.status !== 200) {
      console.log(`Add Payments in error: ${addPaymentsUrl} =>  response: ${addPaymentsResponse.status} - ${addPaymentsResponse.body}`);
      return;
    }
  }

  // Publish the flow
  const publishFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/publish`
  params.tags.api_name = "publish_flow";
  var publishFlowResponse = http.post(publishFlowUrl, {}, params);
  //console.log(`request: None  -  response: ${publishFlowResponse.status} - ${publishFlowResponse.body}`);
  check(publishFlowResponse, {
    'Check if flow was published [HTTP Code: 200]': (_r) => publishFlowResponse.status === 200,
  });
  if (publishFlowResponse.status !== 200) {
    console.log(`Publish flow in error: ${publishFlowUrl} =>  response: ${publishFlowResponse.status} - ${publishFlowResponse.body}`);
  }
}


export function teardown(data) {
  // After All
  // teardown code
}
