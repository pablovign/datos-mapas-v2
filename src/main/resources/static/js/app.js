// Estado global
let departamentos = [];
let opcionesVoto = [];
let selectedDeptos = [];
let graficoDispersion = null;

// Inicialización
document.addEventListener('DOMContentLoaded', async () => {
    inicializarMapas();
    sincronizarMapas();
    configurarEventos();
    await cargarDatosIniciales();
});

async function cargarDatosIniciales() {
    try {
        setLoading(true);
        
        // Cargar departamentos para el dropdown
        departamentos = await fetchDepartamentos();
        renderizarDepartamentos();
        
        // Cargar opciones de voto
        opcionesVoto = await fetchOpcionesVoto();
        actualizarOpcionesVoto();
        
        // Cargar datos iniciales - circuitos sin filtrar por opción de voto
        const circuitosData = await fetchCircuitosVotos(null, 'AFIRMATIVOS', null);
        const radiosData = await fetchRadiosNbi();
        
        renderizarCircuitos(circuitosData);
        renderizarRadios(radiosData);
        actualizarTituloMapaDerecho('Radios Censales (% NBI)');
        limpiarResultadosAnaliticos();
        
    } catch (error) {
        console.error('Error cargando datos:', error);
        alert('Error al cargar los datos. Ver consola para detalles.');
    } finally {
        setLoading(false);
    }
}

function configurarEventos() {
    // Dropdown departamentos
    const deptoBtn = document.getElementById('deptoBtn');
    const deptoMenu = document.getElementById('deptoMenu');
    
    deptoBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        deptoMenu.classList.toggle('show');
    });
    
    // Cerrar dropdown al hacer click fuera
    document.addEventListener('click', () => {
        deptoMenu.classList.remove('show');
    });
    
    deptoMenu.addEventListener('click', (e) => {
        e.stopPropagation();
    });
    
    // Cambio de universo
    document.getElementById('universoSelect').addEventListener('change', actualizarOpcionesVoto);
    
    // Botones
    document.getElementById('aplicarBtn').addEventListener('click', aplicarFiltros);
    document.getElementById('limpiarBtn').addEventListener('click', limpiarFiltros);
}

function renderizarDepartamentos() {
    const menu = document.getElementById('deptoMenu');
    menu.innerHTML = '';
    
    departamentos.forEach(depto => {
        const item = document.createElement('label');
        item.className = 'dropdown-item';
        item.innerHTML = `
            <input type="checkbox" value="${depto.id}">
            <span>${depto.nombre}</span>
        `;
        
        const checkbox = item.querySelector('input');
        checkbox.addEventListener('change', () => {
            if (checkbox.checked) {
                selectedDeptos.push(depto.id);
            } else {
                selectedDeptos = selectedDeptos.filter(id => id !== depto.id);
            }
            actualizarContadorDeptos();
        });
        
        menu.appendChild(item);
    });
}

function actualizarContadorDeptos() {
    const count = selectedDeptos.length;
    const btn = document.getElementById('deptoBtn');
    const countSpan = document.getElementById('deptoCount');
    
    if (count === 0) {
        btn.textContent = 'Seleccionar...';
    } else if (count === 1) {
        btn.textContent = '1 seleccionado';
    } else {
        btn.textContent = `${count} seleccionados`;
    }
    countSpan.textContent = `${count} seleccionados`;
}

function actualizarOpcionesVoto() {
    const universo = document.getElementById('universoSelect').value;
    const select = document.getElementById('opcionSelect');
    
    select.innerHTML = '<option value="">Seleccionar...</option>';
    
    let opcionesFiltradas;
    if (universo === 'AFIRMATIVOS') {
        opcionesFiltradas = opcionesVoto.filter(o => o.esAfirmativo);
    } else if (universo === 'EFECTIVOS') {
        opcionesFiltradas = opcionesVoto;
    } else if (universo === 'HABILITADOS') {
        opcionesFiltradas = [...opcionesVoto, { id: -1, nombre: 'Abstención' }];
    }
    
    opcionesFiltradas.forEach(opcion => {
        const option = document.createElement('option');
        option.value = opcion.id;
        option.textContent = opcion.nombre;
        select.appendChild(option);
    });
}

async function aplicarFiltros() {
    const universo = document.getElementById('universoSelect').value;
    const opcionVotoId = document.getElementById('opcionSelect').value;
    
    try {
        setLoading(true);
        
        const circuitosData = await fetchCircuitosVotos(
            selectedDeptos.length > 0 ? selectedDeptos : null,
            universo,
            opcionVotoId ? parseInt(opcionVotoId, 10) : null
        );

        renderizarCircuitos(circuitosData);
        renderizarCircuitosNbi(circuitosData);
        actualizarTituloMapaDerecho('Circuitos Electorales (% NBI)');
        actualizarAnalisisCorrelacion(circuitosData);
        
    } catch (error) {
        console.error('Error aplicando filtros:', error);
        alert('Error al aplicar filtros.');
    } finally {
        setLoading(false);
    }
}

function limpiarFiltros() {
    // Limpiar seleccionados
    selectedDeptos = [];
    
    // Limpiar checkboxes
    const checkboxes = document.querySelectorAll('#deptoMenu input[type="checkbox"]');
    checkboxes.forEach(cb => cb.checked = false);
    
    // Resetear selects
    document.getElementById('universoSelect').value = 'AFIRMATIVOS';
    document.getElementById('opcionSelect').innerHTML = '<option value="">Seleccionar...</option>';
    
    // Actualizar UI
    actualizarContadorDeptos();
    actualizarOpcionesVoto();
    
    // Recargar datos iniciales
    cargarDatosIniciales();
}

function actualizarTituloMapaDerecho(titulo) {
    const tituloEl = document.getElementById('tituloMapaDerecho');
    if (tituloEl) {
        tituloEl.textContent = titulo;
    }
}

function actualizarAnalisisCorrelacion(circuitosData) {
    const series = extraerSeriesParaAnalisis(circuitosData);
    const metadata = circuitosData?.metadata || {};

    if (series.votos.length < 2) {
        setCorrelacion('--', 'Sin datos suficientes para correlacionar');
        renderizarGraficoVacio();
        return;
    }

    const correlacionBackend = Number(metadata.correlacion);
    const pendienteBackend = Number(metadata.regresionPendiente);
    const interceptoBackend = Number(metadata.regresionIntercepto);

    const tieneCorrelacionBackend = Number.isFinite(correlacionBackend);
    const tieneRegresionBackend = Number.isFinite(pendienteBackend) && Number.isFinite(interceptoBackend);

    const correlacion = tieneCorrelacionBackend
        ? correlacionBackend
        : calcularCorrelacionPearson(series.votos, series.nbi);

    const interpretacionBackend = typeof metadata.interpretacion === 'string' && metadata.interpretacion.trim() !== ''
        ? metadata.interpretacion
        : null;

    const interpretacion = interpretacionBackend || (correlacion !== null && !Number.isNaN(correlacion)
        ? interpretarPearson(correlacion)
        : 'Sin variacion suficiente para calcular correlacion');

    const regresion = tieneRegresionBackend
        ? { pendiente: pendienteBackend, intercepto: interceptoBackend }
        : calcularRegresionLineal(series.votos, series.nbi);

    if (correlacion === null || Number.isNaN(correlacion)) {
        setCorrelacion('--', interpretacion);
    } else {
        setCorrelacion(correlacion.toFixed(2), interpretacion);
    }

    renderizarGraficoDispersion(series.votos, series.nbi, regresion);
}

function extraerSeriesParaAnalisis(circuitosData) {
    const features = Array.isArray(circuitosData?.features) ? circuitosData.features : [];
    const votos = [];
    const nbi = [];

    features.forEach(feature => {
        const props = feature?.properties || {};
        const voto = Number(props.porcentajeVoto);
        const nbiVal = Number(props.porcentajeNbi);

        if (Number.isFinite(voto) && Number.isFinite(nbiVal)) {
            votos.push(voto);
            nbi.push(nbiVal);
        }
    });

    return { votos, nbi };
}

function calcularCorrelacionPearson(x, y) {
    const n = x.length;
    if (n < 2 || n !== y.length) return null;

    const meanX = x.reduce((acc, val) => acc + val, 0) / n;
    const meanY = y.reduce((acc, val) => acc + val, 0) / n;

    let numerador = 0;
    let sumaX = 0;
    let sumaY = 0;

    for (let i = 0; i < n; i += 1) {
        const dx = x[i] - meanX;
        const dy = y[i] - meanY;
        numerador += dx * dy;
        sumaX += dx * dx;
        sumaY += dy * dy;
    }

    const denominador = Math.sqrt(sumaX * sumaY);
    if (denominador === 0) return null;

    return numerador / denominador;
}

function calcularRegresionLineal(x, y) {
    const n = x.length;
    if (n < 2 || n !== y.length) return null;

    const meanX = x.reduce((acc, val) => acc + val, 0) / n;
    const meanY = y.reduce((acc, val) => acc + val, 0) / n;

    let numerador = 0;
    let denominador = 0;

    for (let i = 0; i < n; i += 1) {
        const dx = x[i] - meanX;
        numerador += dx * (y[i] - meanY);
        denominador += dx * dx;
    }

    if (denominador === 0) return null;

    const pendiente = numerador / denominador;
    const intercepto = meanY - (pendiente * meanX);
    return { pendiente, intercepto };
}

function renderizarGraficoDispersion(votos, nbi, regresion) {
    const canvas = document.getElementById('graficoDispersion');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');

    if (graficoDispersion) {
        graficoDispersion.destroy();
    }

    const puntos = votos.map((v, i) => ({ x: v, y: nbi[i] }));

    const datasets = [{
        label: 'Circuitos',
        data: puntos,
        backgroundColor: 'rgba(44, 82, 130, 0.6)',
        borderColor: 'rgba(44, 82, 130, 1)',
        pointRadius: 5
    }];

    if (regresion) {
        const minX = Math.min(...votos);
        const maxX = Math.max(...votos);
        datasets.push({
            type: 'line',
            label: 'Línea de regresión',
            data: [
                { x: minX, y: (regresion.pendiente * minX) + regresion.intercepto },
                { x: maxX, y: (regresion.pendiente * maxX) + regresion.intercepto }
            ],
            borderColor: '#c53030',
            backgroundColor: '#c53030',
            borderWidth: 2,
            pointRadius: 0,
            fill: false,
            tension: 0
        });
    }

    graficoDispersion = new Chart(ctx, {
        type: 'scatter',
        data: { datasets },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: '% Votos'
                    },
                    min: 0,
                    max: 100
                },
                y: {
                    title: {
                        display: true,
                        text: '% NBI'
                    },
                    min: 0,
                    max: 100
                }
            },
            plugins: {
                legend: {
                    display: true
                }
            }
        }
    });
}

function renderizarGraficoVacio() {
    const canvas = document.getElementById('graficoDispersion');
    if (!canvas) return;

    if (graficoDispersion) {
        graficoDispersion.destroy();
        graficoDispersion = null;
    }

    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
}

function setCorrelacion(valor, texto) {
    const valorEl = document.getElementById('correlacionValor');
    const textoEl = document.getElementById('correlacionTexto');
    if (valorEl) valorEl.textContent = valor;
    if (textoEl) textoEl.textContent = texto;
}

function interpretarPearson(r) {
    const absR = Math.abs(r);
    if (absR >= 0.7) return `Correlación fuerte ${r > 0 ? 'positiva' : 'negativa'}`;
    if (absR >= 0.4) return `Correlación moderada ${r > 0 ? 'positiva' : 'negativa'}`;
    if (absR >= 0.2) return 'Correlación débil';
    return 'Correlación nula';
}

function limpiarResultadosAnaliticos() {
    setCorrelacion('--', 'Aplicá filtros para calcular');
    renderizarGraficoVacio();
}

function setLoading(loading) {
    const body = document.body;
    if (loading) {
        body.classList.add('loading');
    } else {
        body.classList.remove('loading');
    }
}
