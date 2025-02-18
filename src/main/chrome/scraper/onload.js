function extractMktDepth() {
  var code = $("#ctl00_BodyPlaceHolder_QuoteSearchView1_ucBuySellQuoteHeader_ucBuySellBar_lblCode_field")[0].textContent
  var price = $("#ctl00_BodyPlaceHolder_QuoteSearchView1_ucBuySellQuoteHeader_ucBuySellBar_lblLast_field")[0].textContent
  var change = $("#ctl00_BodyPlaceHolder_QuoteSearchView1_ucBuySellQuoteHeader_ucBuySellBar_lblChange_field")[0].textContent
  var volume = $("#ctl00_BodyPlaceHolder_QuoteSearchView1_ucShareQuote_lblVolume_field")[0].textContent
  // tables[1].tFoot.children[0].children[0].textContent // "93 buyers for 1,710,134 units"
  // tables[1].tFoot.children[0].children[1].textContent // "73 sellers for 2,445,504 units"
  var tables = $(document).find("table");
  var buyersStr = decomposeMktDepthString(tables[1].tFoot.children[0].children[0].textContent);
  var sellersStr = decomposeMktDepthString(tables[1].tFoot.children[0].children[1].textContent);
  var buyers=buyersStr[0];
  var buyerShares=buyersStr[1];
  var sellers=sellersStr[0];
  var sellerShares=sellersStr[1];
  var data = JSON.stringify({ 'buyers':buyers, 'buyerShares':buyerShares, 'sellers':sellers, 'sellerShares':sellerShares, 'price':parseFloat(price), 'change':parseFloat(change), 'volume':sanitiseInt(volume) });
  console.info("code: "+code+" data: "+data);
  $.ajax({
    url: 'http://localhost:8080/mktDepth/sync/ASX:'+code,
    type: 'POST',
    contentType: "application/json",
    data: data
  });
}
function decomposeMktDepthString(str) {
  let regex = /[0-9,]+/g;
  var x = str.match(regex);
  return [ parseInt(x[0].replace(/,/g, '')), parseInt(x[1].replace(/,/g, '')) ];
}
function sanitiseInt(str) {
  return parseInt(str.replace(/,/g, ''));
}
$(window).on("load",extractMktDepth);

