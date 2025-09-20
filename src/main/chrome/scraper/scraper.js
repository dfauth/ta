function blah() {
  chrome.tabs.query({currentWindow: true, active: true}, function(tabs) {
     const tab = tabs[0];
        chrome.scripting.executeScript({
            target: { tabId: tab.id },
            files: ['jquery-3.7.1.min.js','launch.js'],
        }).then(() => console.log('Injected a function!'));
  })
}
function popupInput() {
   let codes = prompt("gimme codes:", "ASX codes")
   var l = codes.split('\r').map(c => c.split(':')[1])
   console.log('codes are '+l)
   doit(l)
}
var quoteWindow = null;
function doit(codes) {
        codes.forEach(c => {
            var tabUrl = "https://sharetrading.westpac.com.au/Private/MarketPrices/QuoteSearch/QuoteSearch.aspx?stockCode="+c;
            if(quoteWindow == null) {
              quoteWindow = chrome.windows.create({ url: tabUrl});
            } else {
              quoteWindow.then(w => {
                   (async () => {
                       await chrome.tabs.create({windowId: w.id, url: tabUrl});
                   })();
              });
            }
        });
//    const [head, ...tail] = codes
//    chrome.windows.create({
//        url: "https://sharetrading.westpac.com.au/Private/MarketPrices/QuoteSearch/QuoteSearch.aspx?stockCode="+head,
//        type: "popup"
//    }).then(w => {
//        console.log('window id is '+w.id)
//        tail.forEach(c => {
//            var url = "https://sharetrading.westpac.com.au/Private/MarketPrices/QuoteSearch/QuoteSearch.aspx?stockCode="+c;
//            (async () => {
//                console.log('window id is '+w.id+' url is '+url)
//                await chrome.tabs.create({windowId: w.id, url: url});
//            })();
//        });
//    })
};
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
function retrieveCodes() {
  let codes = $.ajax({
    url: 'http://localhost:8080/rank',
    type: 'GET',
    contentType: "application/json",
    success: function(result){
      $("#result").html(JSON.stringify(result));
    }
  });
}
document.getElementById('scraper').addEventListener('click', () => blah());
document.getElementById('custom').addEventListener('click', () => popupInput());

var quoteWindow = null;
chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    var tabUrl = request.url;
    if (tabUrl) {
        if(quoteWindow == null) {
          quoteWindow = chrome.windows.create({ url: tabUrl});
        } else {
          quoteWindow.then(w => {
               (async () => {
                   await chrome.tabs.create({windowId: w.id, url: tabUrl});
                   sendResponse({complete: tabURL});
               })();
          });
        }
    }
  }
);

