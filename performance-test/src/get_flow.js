import http from "k6/http";
import { randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { createInsertAndPublishFlowSetup } from './modules/helper.js';
import { buildCreateFlowRequest, buildAddPaymentsRequest } from './modules/request_builder.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
const varsArray = new SharedArray("vars", function () {
  return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
const vars = varsArray[0];


// initialize parameters taken from env
const fdrBaseUrl = `${vars.base_path}`;
const fdrBaseUrlOrg = `${vars.base_path_org}`;
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

const subscriptionKeyPSP = `${__ENV.API_SUBSCRIPTION_KEY_PSP}`;
const subscriptionKeyOrg = `${__ENV.API_SUBSCRIPTION_KEY_ORG}`;

// Predefined organizations ID
const predefinedOrgIds = ["88888888888", "15376371009"];

export function setup() {
  var params = {
    tags: { 'api_name': '' },
    headers: {
      'Content-Type': 'application/json',
      'Ocp-Apim-Subscription-Key': subscriptionKeyPSP
    },
  };
  // Before All
  for (let i = 0; i < 3; i++) {
    requestValues.creditorInstitutionDomainId = predefinedOrgIds[i];
    requestValues.creditorInstitutionName = predefinedOrgIds[i];
    requestValues.creditorInstitutionId = predefinedOrgIds[i];

    // Call the method for each predefined requestValues
    createInsertAndPublishFlowSetup(requestValues, paymentsInFlow, totalAmount, fdrBaseUrl, params);
  }
}

export default function () {
  var params = {
    tags: { 'api_name': '' },
    headers: {
      'Content-Type': 'application/json',
      'Ocp-Apim-Subscription-Key': subscriptionKeyOrg
    },
  };

  const selectedId = randomItem(predefinedOrgIds);
  requestValues.creditorInstitutionDomainId = selectedId;
  requestValues.creditorInstitutionName = selectedId;
  requestValues.creditorInstitutionId = selectedId;
  console.log(`Selected organization: ${selectedId}`);

  // Get flow list
  const now = new Date();
  now.setHours(0, 0, 0, 0); // publishedGt set to today at 00:00:00
  const isoDate = now.toISOString();
  const getFlowListURL = `${fdrBaseUrlOrg}/organizations/${requestValues.creditorInstitutionId}/fdrs?publishedGt=${isoDate}`;
  params.tags.api_name = "get_flow_list";
  var getFlowListResponse = http.get(getFlowListURL, params);
  check(getFlowListResponse, {
    'Check if the flow list has been got [HTTP Code: 200]': (r) => r.status === 200,
  });
  if (getFlowListResponse.status !== 200) {
    console.log(`Get flow list URL: ${getFlowListURL}  -  response: ${getFlowListResponse.status}`);
  }
  const responseData = JSON.parse(getFlowListResponse.body);
  // Extract all fdr, pspId and revision into an array of objects
  const fdrPspIdRevisionArray = responseData.data.map(item => ({
    fdr: item.fdr,
    pspId: item.pspId,
    revision: item.revision
  }));

  const baseFlowUrlTemplate = `${fdrBaseUrlOrg}/organizations/${requestValues.creditorInstitutionId}/fdrs/{fdr}/revisions/{revision}/psps/{pspId}`;

  // Iterate over the fdrPspIdRevisionArray and make a GET request for each item
  fdrPspIdRevisionArray.forEach(item => {
    // Replace placeholders in the URL template with actual values
    const getFlowURL = baseFlowUrlTemplate
      .replace('{fdr}', item.fdr)
      .replace('{revision}', item.revision)
      .replace('{pspId}', item.pspId);

    // Make the GET request for each constructed URL
    params.tags.api_name = "get_flow";
    const flowRes = http.get(getFlowURL, params);

    // Check if the response is successful for the flow URL
    check(flowRes, {
      'Check if the flow has been got [HTTP Code: 200]': (r) => r.status === 200,
    });
    if (flowRes.status !== 200) {
      console.log(`Response for ${getFlowURL}: ${flowRes.status}`);
    }
    
    // Make the GET request (for payments) for each constructed URL
    const getPaymentsURL = `${getFlowURL}/payments`;
    let pageNumber = 1;
    let totPage = 1;

    params.tags.api_name = "get_payments";
    // Loop through all pages while the current page number is less than or equal to total pages
    while (pageNumber <= totPage) {
      const paymentsRes = http.get(`${getPaymentsURL}?pageNumber=${pageNumber}`, params);
      
      check(paymentsRes, {
        'Check if the payment page has been got [HTTP Code: 200]': (r) => r.status === 200,
      });
      if (paymentsRes.status !== 200) {
        console.log(`Response for ${getPaymentsURL}: ${paymentsRes.status}`);
      }
      
      // Parse the response for the current page
      const paymentsData = JSON.parse(paymentsRes.body);
      // Update the pageNumber and totPage based on the metadata from the response
      pageNumber = paymentsData.metadata.pageNumber + 1;
      totPage = paymentsData.metadata.totPage;
    }
  });
}


export function teardown(data) {
  // After All
  // teardown code
}
