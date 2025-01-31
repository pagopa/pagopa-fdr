import http from "k6/http";
import {check} from 'k6';
import {SharedArray} from 'k6/data';
import { generateFlowNameAndDate } from './modules/helper.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
const varsArray = new SharedArray("vars", function () {
  return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
const vars = varsArray[0];


// initialize parameters taken from env
const fdrBaseUrl = `${vars.base_path}`;
const pspType = `${vars.psp_type}`;
const pspId = `${vars.psp_id}`;
const pspDomainId = `${vars.psp_domain_id}`;
const pspName = `${vars.psp_name}`;
const pspBrokerId = `${vars.psp_broker_id}`;
const channelId = `${vars.channel_id}`;
const creditorInstitutionId = `${vars.creditor_institution_id}`;
const creditorInstitutionDomainId = `${vars.creditor_institution_domain_id}`;
const creditorInstitutionName = `${vars.creditor_institution_name}`;
const subscriptionKey = `${__ENV.API_SUBSCRIPTION_KEY}`;



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

export default function () {

  setup();

  // Create a new flow.
  console.log(`VU: ${__VU}  -  ITER: ${__ITER}`);
  let flowNameAndDate = generateFlowNameAndDate(pspId, `${__VU}`);


  // execute the call and check the response
  const createFlowUrl = `{fdrBaseUrl}/`
  var response = http.post(createFlowUrl, payload, params);

}








const createFlowPayload = JSON.parse(open(`./helpers/payloads.json`)).create_flow_payload;
const addPaymentsPayload = JSON.parse(open(`./helpers/payloads.json`)).add_payments_payload;

let params = {};

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
