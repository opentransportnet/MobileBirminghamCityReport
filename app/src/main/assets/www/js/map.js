var lat = Activity.getLatitude();
var lng = Activity.getLongitude();
var map = L.map('map', {zoomControl: false}).setView([lat, lng], 11);
var locationZoom = 16;
var maxZoom = 16;
var markerLat = 0;
var markerLng = 0;
var percentage = 0;
var zooming = false;
var markerIcon = L.icon({
    iconUrl: 'img/actual_place.png',
    iconSize: [21, 21], // size of the icon
    iconAnchor: [10, 10], // point of the icon which will correspond to marker's location
});
var marker = L.marker([lat, lng], {icon: markerIcon});
var reportPinIcon = L.icon({
    iconUrl: 'img/some-place.png',
    iconSize: [50, 50], // size of the icon
    iconAnchor: [25, 50] // point of the icon which will correspond to marker's location
});
var reportPin = L.marker([lat, lng], {icon: reportPinIcon});
var tileLayer = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a>',
    maxZoom: maxZoom
}).addTo(map);

function currentLocation() {
    var latitude = Activity.getLatitude();
    var longitude = Activity.getLongitude();
    map.setView([latitude, longitude], locationZoom, {pan: {animate: true}});
    MainActivity.closeSearch();
}

document.getElementById("current_location").addEventListener("click",
        currentLocation);

function continueShowLoc() {
    var latitude = Activity.getLatitude();
    var longitude = Activity.getLongitude();

    marker.setLatLng([latitude, longitude]).update();
    lat = latitude;
    lng = longitude;
    
    showMyLocation();
}

function showMyLocation() {
    markerLat = lat;
    markerLng = lng;
    lat = Activity.getLatitude();
    lng = Activity.getLongitude();

    if (markerLat !== lat || markerLng !== lng) {
        percentage = 0.1;
        
        function animateMarker() {
            if (percentage > 1) {
                showMyLocation();
            } else {
                if (!zooming) {
                    var pLat = markerLat + percentage * (lat - markerLat);
                    var pLng = markerLng + percentage * (lng - markerLng);
                    // Update marker location
                    marker.setLatLng([pLat, pLng]).update();
                }
                percentage = percentage + 0.1;
                setTimeout(animateMarker, 50);
            }
        }
        animateMarker();
    }
    else {
        setTimeout(showMyLocation, 1000);
    }
}

setTimeout(function () {
    // Center map
    map.setView([lat, lng], locationZoom, {pan: {animate: false}});
    // Add report flag
    reportPin.addTo(map).setZIndexOffset(100);
    // Add current position marcenterMapker
    //marker.addTo(map).bindPopup('You are here').openPopup();
    marker.addTo(map);
}, 400);

map.on('zoomstart', function (e) {
    zooming = true;
});

map.on('zoomend', function (e) {
    zooming = false;
});

function loadScript(url, callback) {
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    script.onreadystatechange = callback;
    script.onload = callback;
    head.appendChild(script);
}

function loadStyleSheet(url, callback) {
    var head = document.getElementsByTagName('head')[0];
    var styleSheet = document.createElement('link');
    styleSheet.rel = 'stylesheet';
    styleSheet.href = url;
    styleSheet.onreadystatechange = callback;
    styleSheet.onload = callback;
    head.appendChild(styleSheet);
}

map.on('move', function () {
    reportPin.setLatLng(map.getCenter());
});

function openReportDetails(issueId) {
    var latLng = reportPin.getLatLng();
    MainActivity.openReportDetails(issueId, latLng.lat, latLng.lng);
}

function centerMap(lat, lng) {
    map.setView([lat, lng], locationZoom, {pan: {animate: true}});
}