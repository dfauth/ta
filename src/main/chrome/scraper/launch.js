function launch() {
  const regex = /=([0-9A-Z]*)$/;
  $('a').each(function() {
    var href = $(this).attr('href') || '';
//    debugger;

    if (href.startsWith('/Private/MarketPrices/QuoteSearch/QuoteSearch.aspx?stockCode=')) {
      var r = href.match(regex);
      if(r != null && r[1].length > 0) {
          var msg = {url: "https://sharetrading.westpac.com.au/Private/MarketPrices/QuoteSearch/QuoteSearch.aspx?stockCode="+r[1]};
//          chrome.runtime.sendMessage(msg);
          (async () => {
            // block waiting for the response
            const response = await chrome.runtime.sendMessage(msg);
          })();
      }
    }
  });
};
launch();