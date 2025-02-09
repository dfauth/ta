// content.js

// Ensure it's injected only once
var injected;
if(!injected) {
  injected = true;
  chrome.runtime.onMessage.addListener(handleMessage);
}

function handleMessage(message, sender, sendResponse) {
// tables[1].tFoot.children[0].children[0].textContent // "93 buyers for 1,710,134 units"
// tables[1].tFoot.children[0].children[1].textContent // "73 sellers for 2,445,504 units"
  var tables = $(document).find("table");
var buyers = tables[1].tFoot.children[0].children[0].textContent
var sellers = tables[1].tFoot.children[0].children[1].textContent
  sendResponse([buyers,sellers]);
  console.info("buyers: "+buyers);
  console.info("sellers: "+sellers);
}
