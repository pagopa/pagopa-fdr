import http from "k6/http";

export function generateFlowNameAndDate(pspId, virtualUser) {
    const today = new Date();
    const todayISO = new Date().toISOString();
    const formattedDate = todayISO.slice(0, 10);
    const timestampWithoutFirstDigits = Date.now().toString().slice(3);
    const formattedVU = virtualUser.padStart(3, "0");
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