var map = L.map('map', {
        zoomControl: false,
        attributionControl: false
    });

function showLoc(lat, lng) {
    map.setView([lat, lng], 16);

    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {maxZoom: 16, })
            .addTo(map);

    L.marker([lat, lng]).addTo(map);
}
