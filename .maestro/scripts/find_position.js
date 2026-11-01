// The feed reports coordinates as whole numbers: degrees times one million.
var lat = parseFloat(output.testLat) * 1000000;
var lon = parseFloat(output.testLon) * 1000000;
var entries = json(http.get('https://api-cdn.criticalmaps.net/locations').body);

output.position = {
    found: entries.some(function (entry) {
        return Math.abs(entry.latitude - lat) <= 5 && Math.abs(entry.longitude - lon) <= 5;
    })
};
