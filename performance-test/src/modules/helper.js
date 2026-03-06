import http from "k6/http";
import { buildCreateFlowRequest, buildAddPaymentsRequest } from './request_builder.js';

export function generateFlowNameAndDate(pspId, virtualUser) {
    const today = new Date();
    const todayISO = new Date().toISOString();
    const formattedDate = todayISO.slice(0, 10);
    const timestampWithoutFirstDigits = Date.now().toString().slice(3);
    const fdrName = `${formattedDate}${pspId}-${timestampWithoutFirstDigits}${virtualUser}`;
    const fdrDate = todayISO.slice(0, -1) + 'Z';
    return [fdrName, fdrDate];
}

export function generateFlowNameDateOrgs(pspId, creditorInstitutions, virtualUser) {
    const fdrNameDate = generateFlowNameAndDate(pspId, virtualUser);
    // pseudo-random number (Mulberry32)
    const prng = (s) => {
        return function() {
            let t = s += 0x6D2B79F5;
            t = Math.imul(t ^ t >>> 15, t | 1);
            t ^= t + Math.imul(t ^ t >>> 7, t | 61);
            return ((t ^ t >>> 14) >>> 0) / 4294967296;
        };
    };
    const random = prng(virtualUser)(); // generate a random number between 0 and 1
    const index = Math.floor(random * creditorInstitutions.length);
    return [fdrNameDate[0], fdrNameDate[1], creditorInstitutions[index]];
}

export function generatePartitionIndexes(totalElements, partitionSize) {    
    const partitions = [];
    let startIndex = 1;
    const numberOfPartitions = Math.ceil(totalElements / partitionSize);  
    for (let i = 0; i < numberOfPartitions; i++) {
      const endIndex = Math.min(startIndex + partitionSize - 1, totalElements);
      partitions.push({ startIndex, endIndex });
      startIndex = endIndex + 1;
    }  
    return partitions;
}

export function createInsertAndPublishFlowSetup(requestValues, paymentsInFlow, totalAmount, fdrBaseUrl, params) {
    const randomInt = Math.floor(Math.random() * 6) + 1
    let flowNameAndDate = generateFlowNameAndDate(requestValues.pspDomainId, randomInt);
    let flowName = flowNameAndDate[0]
    let flowDate = flowNameAndDate[1]
  
    // Create a new flow
    let createFlowRequest = buildCreateFlowRequest(flowName, flowDate, paymentsInFlow, totalAmount, requestValues);
    const createFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}`
    params.tags.api_name = "create_empty_flow";
    var createFlowResponse = http.post(createFlowUrl, createFlowRequest, params);
    if (createFlowResponse.status !== 201) {
      console.log(`Create flow in error: ${createFlowUrl} => response: ${createFlowResponse.status} - ${createFlowResponse.body}`);
      console.log("CREATED FLOW REQ --> " + createFlowRequest)
      return;
    }
  
    const partitions = generatePartitionIndexes(paymentsInFlow, 1000);
    const addPaymentsUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/payments/add`
    params.tags.api_name = "add_payment";
    for (const partition of partitions) {
      // Add new payments
      let addPaymentsRequest = buildAddPaymentsRequest(partition, 100.00, flowDate);
      var addPaymentsResponse = http.put(addPaymentsUrl, addPaymentsRequest, params);
      if (addPaymentsResponse.status !== 200) {
        console.log(`Add Payments in error: ${addPaymentsUrl} =>  response: ${addPaymentsResponse.status} - ${addPaymentsResponse.body}`);
        return;
      }
    }
  
    // Publish the flow
    const publishFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/publish`
    params.tags.api_name = "publish_flow";
    var publishFlowResponse = http.post(publishFlowUrl, {}, params);
    if (publishFlowResponse.status !== 200) {
      console.log(`Publish flow in error: ${publishFlowUrl} =>  response: ${publishFlowResponse.status} - ${publishFlowResponse.body}`);
    }
}

export function randomIntFromInterval(min, max) {
    // min and max included
    return Math.floor(Math.random() * (max - min + 1) + min);
}

export function getOrganizations(apiCfgService, apiCfgSubscriptionKey, companyName) {
    var params = {
        tags: { 'api_name': '' },
        headers: {
            'Content-Type': 'application/json',
            'Ocp-Apim-Subscription-Key': apiCfgSubscriptionKey
        },
    };
    // pass params (headers/tags) as second argument to http.get and return the response
    const response = http.get(apiCfgService, params);
    const body = JSON.parse(response.body);
    return Object.values(body.creditorInstitutions).filter(ci =>
        //console.log("ci", ci.business_name)
        ci.business_name != null ? ci.business_name.toLowerCase().includes(companyName.toLowerCase()): false
    ).map(ci => {
        return {
            'code': ci.creditor_institution_code,
            'name': ci.business_name
        }
    });
}