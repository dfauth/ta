function blah() {
  chrome.action.onClicked.addListener((tab) => {
    chrome.scripting.executeScript({
     target: {tabId: tab.id},
     files: ["jquery-3.7.1.min.js","content.js"],
    })
  });
}
// blah();
