let mapaCircuitos = null;
let mapaRadios = null;
let layerCircuitos = null;
let layerRadios = null;

function inicializarMapas() {
    // Mapa de Circuitos Electorales (izquierda)
    mapaCircuitos = L.map('mapaCircuitos').setView([-33.2, -65.5], 6);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(mapaCircuitos);

    // Mapa de Radios Censales (derecha)
    mapaRadios = L.map('mapaRadios').setView([-33.2, -65.5], 6);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(mapaRadios);
}

function fixCoordinates(coords) {
    // coords viene como [lon, lat] pero Leaflet necesita [lat, lon]
    if (Array.isArray(coords[0])) {
        // Es un array de coordenadas - llamada recursiva
        return coords.map(c => fixCoordinates(c));
    }
    // Es una coordenada simple [lon, lat] -> [lat, lon]
    return [coords[1], coords[0]];
}

function fixGeoJSON(data) {
    if (!data || !data.features) return data;
    
    const fixed = {
        type: 'FeatureCollection',
        features: data.features.map(f => {
            if (!f.geometry) return f;
            
            return {
                type: 'Feature',
                properties: f.properties,
                geometry: {
                    type: f.geometry.type,
                    coordinates: fixCoordinates(f.geometry.coordinates)
                }
            };
        })
    };
    
    return fixed;
}

function renderizarCircuitos(data) {
    if (layerCircuitos) {
        layerCircuitos.clearLayers();
    }

    if (!data || !data.features || data.features.length === 0) {
        console.warn('No hay datos de circuitos para renderizar');
        return;
    }

    layerCircuitos = L.geoJSON(data, {
        style: feature => {
            const pct = feature.properties?.porcentajeVoto || 0;
            return {
                fillColor: getColorVotos(pct),
                weight: 1.4,
                opacity: 1,
                color: '#334155',
                fillOpacity: 0.58
            };
        },
        onEachFeature: (feature, layer) => {
            const props = feature.properties || {};
            const detalles = Array.isArray(props.votosDetalle) ? props.votosDetalle : [];
            const detalleHtml = detalles.length > 0
                ? detalles
                    .map(item => `${item.nombre || 'Sin nombre'}: ${formatearNumero(item.cantidad)} (${formatearPorcentaje(item.porcentaje)}%)`)
                    .join('<br>')
                : 'Sin datos de votos';

            const contenido = `
                <strong>Circuito: ${props.codigo || 'N/A'}</strong><br>
                Electores: ${formatearNumero(props.electores)}<br>
                <strong>Votos por opción</strong><br>
                ${detalleHtml}
            `;
            layer.bindPopup(contenido);
        }
    }).addTo(mapaCircuitos);

    if (layerCircuitos.getBounds().isValid()) {
        mapaCircuitos.fitBounds(layerCircuitos.getBounds(), { padding: [20, 20] });
    }
}

function renderizarRadios(data) {
    if (layerRadios) {
        layerRadios.clearLayers();
    }

    if (!data || !data.features || data.features.length === 0) {
        console.warn('No hay datos de radios para renderizar');
        return;
    }

    const geoJsonData = data;

    layerRadios = L.geoJSON(geoJsonData, {
        style: feature => {
            const pct = feature.properties?.porcentajeNbi || 0;
            return {
                fillColor: getColorNBI(pct),
                weight: 0.5,
                opacity: 1,
                color: '#444',
                fillOpacity: 0.7
            };
        },
        onEachFeature: (feature, layer) => {
            const props = feature.properties || {};
            const contenido = `
                <strong>Radio: ${props.codigo || 'N/A'}</strong><br>
                Hogares Total: ${formatearNumero(props.hogaresTotal)}<br>
                Hogares NBI: ${formatearNumero(props.hogaresNbi)}<br>
                % NBI: ${formatearPorcentaje(props.porcentajeNbi)}%
            `;
            layer.bindPopup(contenido);
        }
    }).addTo(mapaRadios);

    if (layerRadios.getBounds().isValid()) {
        mapaRadios.fitBounds(layerRadios.getBounds(), { padding: [20, 20] });
    }
}

function renderizarCircuitosNbi(data) {
    if (layerRadios) {
        layerRadios.clearLayers();
    }

    if (!data || !data.features || data.features.length === 0) {
        console.warn('No hay datos de circuitos para NBI');
        return;
    }

    layerRadios = L.geoJSON(data, {
        style: feature => {
            const pct = feature.properties?.porcentajeNbi || 0;
            return {
                fillColor: getColorNBI(pct),
                weight: 1,
                opacity: 1,
                color: '#444',
                fillOpacity: 0.7
            };
        },
        onEachFeature: (feature, layer) => {
            const props = feature.properties || {};
            const contenido = `
                <strong>Circuito: ${props.codigo || 'N/A'}</strong><br>
                Hogares ponderados: ${formatearDecimal(props.hogaresPonderados)}<br>
                Hogares NBI ponderados: ${formatearDecimal(props.hogaresNbiPonderado)}<br>
                % NBI: ${formatearPorcentaje(props.porcentajeNbi)}%
            `;
            layer.bindPopup(contenido);
        }
    }).addTo(mapaRadios);

    if (layerRadios.getBounds().isValid()) {
        mapaRadios.fitBounds(layerRadios.getBounds(), { padding: [20, 20] });
    }
}

function formatearNumero(valor) {
    if (valor === undefined || valor === null || Number.isNaN(valor)) {
        return 'N/A';
    }
    return Number(valor).toLocaleString('es-AR');
}

function formatearDecimal(valor) {
    if (valor === undefined || valor === null || Number.isNaN(valor)) {
        return 'N/A';
    }
    return Number(valor).toLocaleString('es-AR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatearPorcentaje(valor) {
    if (valor === undefined || valor === null || Number.isNaN(valor)) {
        return '--';
    }
    return Number(valor).toLocaleString('es-AR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function getColorVotos(value) {
    return value >= 50 ? '#facc15' :
           value >= 40 ? '#fde047' :
           value >= 30 ? '#fef08a' :
           value >= 20 ? '#fef9c3' :
           value >= 10 ? '#fefce8' :
           '#fffff2';
}

function getColorNBI(value) {
    return value >= 40 ? '#742a2a' :
           value >= 30 ? '#c53030' :
           value >= 20 ? '#e53e3e' :
           value >= 10 ? '#fc8181' :
           value >= 5  ? '#feb2b2' :
           '#fff5f5';
}

function sincronizarMapas() {
    mapaCircuitos.on('zoomend', () => {
        mapaRadios.setZoom(mapaCircuitos.getZoom());
    });
    
    mapaCircuitos.on('moveend', () => {
        mapaRadios.setView(mapaCircuitos.getCenter());
    });
}
