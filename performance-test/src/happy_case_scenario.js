import http from 'k6/http';
import {check} from 'k6';
import {SharedArray} from 'k6/data';
import { generateFlowNameAndDate, createFlow, addPayments, publishFlow, test } from './helpers/fdr_helper.js';

const configObject = JSON.parse(open('config.json'));

const envConfig = new SharedArray('config', function () {
  return JSON.parse(open(`./${configObject.env}.environment.json`)).environment;
});

export const options = JSON.parse(open(`${configObject.testTypeFile}`));

const config = envConfig[0];
const rootUrl = `${config.host}/${config.basePath}`;
const subscriptionKey = `${__ENV.PSP_SUBSCRIPTION_KEY}`;
const pspId = configObject.pspId;

const createFlowPayload = JSON.parse(open(`/helpers/payloads.json`)).create_flow_payload;
const addPaymentsPayload = JSON.parse(open(`/helpers/payloads.json`)).add_payments_payload;

let params = {};

export function setup() {
  // Before All
  params = {
    headers: {
      'Content-Type': 'application/json',
      'Ocp-Apim-Subscription-Key': subscriptionKey
    },
  };
}

function precondition() {
  // Preconditions
}

function postcondition() {
}

export default function () {
  setup();
  // Create a new flow.
  console.log(`VU: ${__VU}  -  ITER: ${__ITER}`);

  let flowNameAndDate = generateFlowNameAndDate(pspId, `${__VU}`);
  let createResponse = createFlow(rootUrl, createFlowPayload, configObject, flowNameAndDate, params);
   console.log("@@@Create response: ", createResponse);
  check(createResponse, {
    'CREATE flow status is 201': (_r) => createResponse.status === 201,
  });

  let addPaymentsResponse = addPayments(rootUrl, addPaymentsPayload, pspId, flowNameAndDate, params);
  console.log("@@@Add Payments response: ", addPaymentsResponse);
  check(addPaymentsResponse, {
    'ADD payments status is 200': (_r) => addPaymentsResponse.status === 200,
  });

  let publishResponse = publishFlow(rootUrl, pspId, flowNameAndDate, params);
  console.log("@@@Publish Flow response: ", publishResponse);
  check(publishResponse, {
    'PUBLISH flow status is 200': (_r) => publishResponse.status === 200,
  });
}

export function teardown(data) {
  // After All
}
