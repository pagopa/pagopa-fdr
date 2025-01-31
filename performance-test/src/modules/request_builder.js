
export function buildCreateFlowRequest(flowName, flowDate, totalPayments, totalAmount, requestValues) {

  var request = {
    fdr: `${flowName}`,
    fdrDate: `${flowDate}`,
    sender: {
      type: `${requestValues.pspType}`,
      id: `${requestValues.pspId}`,
      pspId: `${requestValues.pspDomainId}`,
      pspName:  `${requestValues.pspName}`,
      pspBrokerId: `${requestValues.pspBrokerId}`,
      channelId: `${requestValues.channelId}`,
      password: "PLACEHOLDER"
    },
    receiver: {
      id: `${requestValues.creditorInstitutionId}`,
      organizationId: `${requestValues.creditorInstitutionDomainId}`,
      organizationName: `${requestValues.creditorInstitutionName}`
    },
    regulation: "SEPA - Integration Test",
    regulationDate: `${flowDate}`,
    bicCodePouringBank: "UNCRITMMXXX",
    totPayments: totalPayments,
    sumPayments: totalAmount
  };
  return JSON.stringify(request);
}

export function buildAddPaymentsRequest(partition, paymentAmount, date) {

  const timestamp = Date.now().toString(); 

  var createdPayments = [];
  for (let index = partition.startIndex; index <= partition.endIndex; index++) {

    const paddedIndex = index.toString().padStart(10, '0');
    createdPayments.push({
      index: index,
      iuv: `${timestamp}${paddedIndex}`,
      iur: `0${timestamp}${paddedIndex}`,
      idTransfer: 1,
      pay: paymentAmount.toFixed(2),
      payStatus: "EXECUTED",
      payDate: `${date}`
    })
  }
  return JSON.stringify({
    payments: createdPayments
  });
}