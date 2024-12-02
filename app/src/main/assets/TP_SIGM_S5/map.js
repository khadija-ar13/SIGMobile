// Initialisation de la carte
var map = L.map('mapid').setView([33.808975, -7.045327], 8);
L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap'
}).addTo(map);

// Points de départ et d'arrivée
var point_depart = L.marker([33.9716, -6.8498], {
    draggable: true,
    title: "Depart"
}).addTo(map);

var point_arrive = L.marker([33.573, -7.5898], {
    draggable: true,
    title: "Destination"
}).addTo(map);

// Fonction pour calculer la distance
function calculDistance(latlng1, latlng2) {
    return (latlng1.distanceTo(latlng2) / 1000).toFixed(2);
}

// Gestion des événements de glisser-déposer des marqueurs
point_depart.on('drag', function (e) {
    var distance = calculDistance(e.latlng, point_arrive.getLatLng());
    console.log(distance + " km");
});

point_arrive.on('drag', function (e) {
    var distance = calculDistance(e.latlng, point_depart.getLatLng());
    console.log(distance + " km");
});

// Fonction pour obtenir l'itinéraire
async function fetchData(startCoordinates, endCoordinates) {
    const openRouteServiceURL = 'https://api.openrouteservice.org/v2/directions/driving-car';
    const apiKey = '5b3ce3597851110001cf6248e9df887d16d24d418fd38e6a8e829aa8';
    const headers = new Headers({
        'Accept': 'application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8'
    });

    try {
        const response = await fetch(
            `${openRouteServiceURL}?api_key=${apiKey}&start=${startCoordinates}&end=${endCoordinates}`,
            { method: 'GET', headers: headers }
        );

        if (response.ok) {
            const data = await response.json();
            displayRoute(data);
        } else {
            console.error('Erreur de réponse :', response.status, response.statusText);
            clearRoute();
        }
    } catch (error) {
        console.error('Erreur lors de la récupération de l\'itinéraire :', error);
        clearRoute();
    }
}

// Fonction pour afficher l'itinéraire sur la carte
function displayRoute(routeData) {
    clearRoute();
    const coordinates = routeData.features[0].geometry.coordinates;
    const routePoints = coordinates.map(coord => L.latLng(coord[1], coord[0]));
    const routePolyline = L.polyline(routePoints, { color: 'blue' });
    routePolyline.addTo(map);
    map.fitBounds(routePolyline.getBounds());
}

// Fonction pour effacer l'itinéraire
function clearRoute() {
    map.eachLayer(function (layer) {
        if (layer instanceof L.Polyline) {
            map.removeLayer(layer);
        }
    });
}

// Gestion du formulaire de recherche
document.getElementById('route-form').addEventListener('submit', function (event) {
    event.preventDefault();
    const startCoordinates = document.getElementById('start').value;
    const endCoordinates = document.getElementById('end').value;
    fetchData(startCoordinates, endCoordinates);
});
