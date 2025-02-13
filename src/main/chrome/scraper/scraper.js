function blah() {
  chrome.tabs.query({currentWindow: true, active: true}, function(tabs) {
     const tab = tabs[0];
        chrome.scripting.executeScript({
            target: { tabId: tab.id },
            files: ['jquery-3.7.1.min.js','launch.js'],
        }).then(() => console.log('Injected a function!'));
  })
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

