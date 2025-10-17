import http from "k6/http";
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import {generateFlowNameAndDate, generatePartitionIndexes, randomIntFromInterval} from './modules/helper.js';
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

// const paymentsInFlow = `${__ENV.PAYMENTS_IN_FLOW}`;
// const maxParallelCalls = __ENV.MAX_PARALLEL_CALLS && Number(__ENV.MAX_PARALLEL_CALLS) > 0 ? Number(__ENV.MAX_PARALLEL_CALLS) : 5;
const maxPaymentsInCall = 1000;
// const totalAmount = paymentsInFlow * 100.00;

let totalAnalysis = {
  payments: 0
}

const subscriptionKey = `${__ENV.API_SUBSCRIPTION_KEY_PSP}`;

var params = {};
var data = {};

export function setup() {
  // Before All
  params = {
    tags: { 'api_name': '' },
    headers: {
      'Content-Type': 'application/json',
      'Ocp-Apim-Subscription-Key': subscriptionKey
    },
  };

  let flowNameAndDate = generateFlowNameAndDate(requestValues.pspDomainId, `${__VU}`);

  const paymentsInFlow = randomIntFromInterval(1, `${__ENV.PAYMENTS_IN_FLOW}`);

  totalAnalysis.payments += paymentsInFlow;

  let requests = [];

  const partitions = generatePartitionIndexes(paymentsInFlow, maxPaymentsInCall);
  for (const partition of partitions) {
    requests.push(buildAddPaymentsRequest(partition, 100.00, flowNameAndDate[1]));
  }

  data = {
    flowName: flowNameAndDate[0],
    flowDate: flowNameAndDate[1],
    paymentsInFlow: paymentsInFlow,
    totalAmount: paymentsInFlow * 100.00,
    addPaymentsRequests: requests
  };

}

export default function () {

  setup();

  // Create a new flow
  const flowName = data.flowName;
  const flowDate = data.flowDate;
  const paymentsInFlow = data.paymentsInFlow;
  let createFlowRequest = buildCreateFlowRequest(flowName, flowDate, paymentsInFlow, data.totalAmount, requestValues);
  const createFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}`
  params.tags.api_name = "create_empty_flow";
  var createFlowResponse = http.post(createFlowUrl, createFlowRequest, params);
  check(createFlowResponse, {
    'Check if empty flow was created [HTTP Code: 201]': (_r) => createFlowResponse.status === 201,
  });
  if (createFlowResponse.status !== 201) {
    console.log(`Create flow in error: ${createFlowUrl} => response: ${createFlowResponse.status} - ${createFlowResponse.body}`);
    return;
  }

  // Add all payments in the created flow
  const maxParallelCalls = randomIntFromInterval(3, __ENV.MAX_PARALLEL_CALLS && Number(__ENV.MAX_PARALLEL_CALLS) > 0 ? Number(__ENV.MAX_PARALLEL_CALLS) : 5);
  console.log(`Defining max [${maxParallelCalls}] parallel calls with max [${paymentsInFlow}] payments.`);
  const addPaymentsUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/payments/add`
  params.tags.api_name = "add_payment";
  let requests = [];
  data.addPaymentsRequests.forEach((addPaymentsRequest, index) => {
    if (index % maxParallelCalls === 0) {
      requests.push([]);
    }
    let currentBatchIndex = Math.floor(index / maxParallelCalls);
    requests[currentBatchIndex].push({
      method: 'PUT',
      url: addPaymentsUrl,
      body: addPaymentsRequest,
      params: params,
    });
  });
  requests.forEach(batch => {
    let responses = http.batch(batch);
    responses.forEach((res, index) => {
      check(res, { 'Check if payments were added to flow [HTTP Code: 200]': (r) => r.status === 200 });
      if (res.status !== 200) {
        console.log(`Add Payments in error: ${batch[index].url} => response: ${res.status} - ${res.body}`);
      }
    });
  });

  // Publish the flow
  const publishFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/publish`
  params.tags.api_name = "publish_flow";
  var publishFlowResponse = http.post(publishFlowUrl, {}, params);
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
  console.log(`Total payments added: ${totalAnalysis.payments}`);
}
