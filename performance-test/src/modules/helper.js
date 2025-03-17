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
    console.log(`Response for ${createFlowUrl}: ${createFlowResponse.status}`);
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
      console.log(`Response for ${addPaymentsUrl}: ${addPaymentsResponse.status}`);
      if (addPaymentsResponse.status !== 200) {
        console.log(`Add Payments in error: ${addPaymentsUrl} =>  response: ${addPaymentsResponse.status} - ${addPaymentsResponse.body}`);
        return;
      }
    }
  
    // Publish the flow
    const publishFlowUrl = `${fdrBaseUrl}/psps/${requestValues.pspDomainId}/fdrs/${flowName}/publish`
    params.tags.api_name = "publish_flow";
    var publishFlowResponse = http.post(publishFlowUrl, {}, params);
    console.log(`Response for ${publishFlowUrl}: ${publishFlowResponse.status}`);
    if (publishFlowResponse.status !== 200) {
      console.log(`Publish flow in error: ${publishFlowUrl} =>  response: ${publishFlowResponse.status} - ${publishFlowResponse.body}`);
    }
}