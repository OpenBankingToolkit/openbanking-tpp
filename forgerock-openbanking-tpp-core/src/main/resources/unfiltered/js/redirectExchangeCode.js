function getParameters(location) {
    if (typeof location === 'undefined') {
        location = window.location;
    }
    var hashParams = new (function Params() {
    })();
    if (location.hash.length === 0) {
        return hashParams;
    }
    ;
    var hashArray = location.hash.substring(1).split('&');
    for (var i in hashArray) {
        var keyValPair = hashArray[i].split('=');
        hashParams[keyValPair[0]] = keyValPair[1];
    }
    return hashParams;
}

var tppExchangeCodeUri = $('#tppExchangeCodeUri').val();

if (window.location.hash) {
    var params = getParameters(window.location);
    window.location.replace(tppExchangeCodeUri + "?code=" + params["code"] +
        "&id_token=" + params["id_token"] + "&state=" + params["state"]);
}
