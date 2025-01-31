import http from "k6/http";

export function generateFlowNameAndDate(pspId, VU) {
    const today = new Date();
    const todayISO = new Date().toISOString();
    const formattedDate = todayISO.slice(0, 10);
    let n = "";
    for (let i = 0; i < 11; i++) {
        n += `${Math.floor(Math.random() * 10)}`;
    }
    const fdrName = `${formattedDate}${pspId}-${n}`;
    const fdrDate = todayISO.slice(0, -1) + 'Z';
    return [fdrName, fdrDate];
}

export function createFlow(rootUrl, payload, configObject, flowNameAndDate, params) {
    console.log(`Creating flow with name ${flowNameAndDate[0]}`);
    const url = `${rootUrl}/psps/${configObject.pspId}/fdrs/${flowNameAndDate[0]}`;
    payload.fdr = flowNameAndDate[0];
    payload.fdrDate = flowNameAndDate[1];
    payload.sender.pspId = configObject.pspId;
    payload.regulationDate = flowNameAndDate[1];
    return http.post(url, JSON.stringify(payload), params);
}

export function deleteFlow(rootUrl, pspId, flowNameAndDate, params) {
    console.log(`Deleting flow with name ${flowNameAndDate[0]}`);
    const url = `${rootUrl}/psps/${pspId}/fdrs/${flowNameAndDate[0]}`;
    return http.del(url, null, params);
}

export function getCreatedPayments(rootUrl, configObject, flowNameAndDate, params) {
    console.log(`Getting created payments of flow named: ${flowNameAndDate[0]}`);
    const url = `${rootUrl}/psps/${configObject.pspId}/created/fdrs/${flowNameAndDate[0]}/organizations/${configObject.organizationId}/payments`;
    return http.get(url, params);
}

export function addPayments(rootUrl, payload, pspId, flowNameAndDate, params) {
    const url = `${rootUrl}/psps/${pspId}/fdrs/${flowNameAndDate[0]}/payments/add`;
    return http.put(url, JSON.stringify(payload), params);
}

export function publishFlow(rootUrl, pspId, flowNameAndDate, params) {
    const url = `${rootUrl}/psps/${pspId}/fdrs/${flowNameAndDate[0]}/publish`;
    return http.post(url, null, params);
}