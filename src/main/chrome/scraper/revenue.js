function extractRevenue() {
  var code = $("#ctl00_BodyPlaceHolder_FinancialsView1_ucCompanyProfilesHeader_ucBuySellQuoteHeader_ucBuySellBar_lblCode_field")[0].textContent
  var revenues = [];
  var yr1Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear01_field")[0].textContent
  var yr1 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear01_field")[0].textContent
  isNumber(yr1) && revenues.push({periodEnd: yr1Date, amount: toNumber(yr1)});
  var yr2Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear02_field")[0].textContent
  var yr2 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear02_field")[0].textContent
  isNumber(yr2) && revenues.push({periodEnd: yr2Date, amount: toNumber(yr2)});
  var yr3Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear03_field")[0].textContent
  var yr3 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear03_field")[0].textContent
  isNumber(yr3) && revenues.push({periodEnd: yr3Date, amount: toNumber(yr3)});
  var yr4Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear04_field")[0].textContent
  var yr4 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear04_field")[0].textContent
  isNumber(yr4) && revenues.push({periodEnd: yr4Date, amount: toNumber(yr4)});
  var yr5Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear05_field")[0].textContent
  var yr5 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear05_field")[0].textContent
  isNumber(yr5) && revenues.push({periodEnd: yr5Date, amount: toNumber(yr5)});
  var yr6Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear06_field")[0].textContent
  var yr6 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear06_field")[0].textContent
  isNumber(yr6) && revenues.push({periodEnd: yr6Date, amount: toNumber(yr6)});
  var yr7Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear07_field")[0].textContent
  var yr7 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear07_field")[0].textContent
  isNumber(yr7) && revenues.push({periodEnd: yr7Date, amount: toNumber(yr7)});
  var yr8Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear08_field")[0].textContent
  var yr8 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear08_field")[0].textContent
  isNumber(yr8) && revenues.push({periodEnd: yr8Date, amount: toNumber(yr8)});
  var yr9Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear09_field")[0].textContent
  var yr9 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear09_field")[0].textContent
  isNumber(yr9) && revenues.push({periodEnd: yr9Date, amount: toNumber(yr9)});
  var yr10Date = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_lblYear10_field")[0].textContent
  var yr10 = $("#ctl00_BodyPlaceHolder_FinancialsView1_ctl01_ctl00_ctl01_lblYear10_field")[0].textContent
  isNumber(yr10) && revenues.push({periodEnd: yr10Date, amount: toNumber(yr10)});
  var data = JSON.stringify({revenue: revenues});
  console.info("code: "+code+" data: "+data);
  $.ajax({
    url: 'http://localhost:8081/api/fa/ASX:'+code,
    type: 'POST',
    contentType: "application/json",
    data: data
  });
}
function isNumber(str) {
  return str.trim() !== "" && isFinite(Number(str.replace(/,/g, "")));
}
function toNumber(str) {
  return Number(str.replace(/,/g, ""));
}
$(window).on("load",extractRevenue);

