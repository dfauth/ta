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
   let codes = prompt("gimme codes:", "ASX:WGX\rASX:SPZ\rASX:PAR\rASX:BGL\rASX:VEE\rASX:4DX")
   var l = codes.split('\r').map(c => c.split(':')[1])
   console.log('codes are '+l)
   doit(l)
}
function quoteUrl(c) {
    return "https://sharetrading.westpac.com.au/Private/MarketPrices/QuoteSearch/QuoteSearch.aspx?stockCode="+c;
}
function doit(codes) {
    var it = codes[Symbol.iterator]()
    var c = it.next();
    if(!c.done) {
        var quoteWindow = chrome.windows.create({url: quoteUrl(c.value)});
        quoteWindow.then(w => {
            let intervalId = setInterval(() => {
                var c1 = it.next();
                if(!c1.done) {
                    (async () => {
                        chrome.tabs.query({windowId: w.id}, function (tabs) {
                            chrome.tabs.update(tabs[0].id, {url: quoteUrl(c1.value)});
                        });
                     })();
                } else {
                    clearInterval(intervalId);
                }
            }, 3000);
        });
    }
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
var n = 1;
chrome.runtime.onMessage.addListener(
  function(request, sender, sendResponse) {
    var tabUrl = request.url;
    if (tabUrl) {
        if(quoteWindow == null) {
          quoteWindow = chrome.windows.create({ url: tabUrl});
        } else {
            setTimeout(() => {
                quoteWindow.then(w => {
                    chrome.tabs.query({windowId: w.id}, function (tabs) {
                        chrome.tabs.update(tabs[0].id, {url: tabUrl});
                    });
                })
            }, 3000 * n++);
        }
    }
  }
);

