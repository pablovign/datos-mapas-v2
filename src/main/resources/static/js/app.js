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
        actualizarTitulosMapas('Circuitos Electorales', 'Radios Censales (% NBI)');
        limpiarResultadosAnaliticos();
        
    } catch (error) {
        console.error('Error cargando datos:', error);
        alert('Error al cargar los datos. Ver consola para detalles.');
    } finally {
        setLoading(false);
    }
}

function configurarEventos() {
    // Cambio de universo
    document.getElementById('universoSelect').addEventListener('change', () => {
        actualizarOpcionesVoto();
        actualizarEstadoBotonAplicar();
    });
    
    // Cambio de opción de voto
    document.getElementById('opcionSelect').addEventListener('change', actualizarEstadoBotonAplicar);
    
    // Botones
    document.getElementById('aplicarBtn').addEventListener('click', aplicarFiltros);
    document.getElementById('limpiarBtn').addEventListener('click', limpiarFiltros);
}

function actualizarEstadoBotonAplicar() {
    const universo = document.getElementById('universoSelect').value;
    const opcionVoto = document.getElementById('opcionSelect').value;
    const hayDepartamento = selectedDeptos.length > 0;
    const hayUniverso = universo !== '';
    const hayOpcion = opcionVoto !== '';
    
    const boton = document.getElementById('aplicarBtn');
    if (boton) {
        boton.disabled = !(hayDepartamento && hayUniverso && hayOpcion);
    }
}

function renderizarDepartamentos() {
    const container = document.getElementById('deptoCheckboxes');
    container.innerHTML = '';
    
    departamentos.forEach(depto => {
        const item = document.createElement('label');
        item.className = 'checkbox-item';
        item.innerHTML = `
            <input type="checkbox" value="${depto.id}">
            <span>${escapeHtml(depto.nombre)}</span>
        `;
        
        const checkbox = item.querySelector('input');
        checkbox.addEventListener('change', () => {
            if (checkbox.checked) {
                selectedDeptos.push(depto.id);
            } else {
                selectedDeptos = selectedDeptos.filter(id => id !== depto.id);
            }
            actualizarEstadoBotonAplicar();
        });
        
        container.appendChild(item);
    });
}

function actualizarOpcionesVoto() {
    const universo = document.getElementById('universoSelect').value;
    const select = document.getElementById('opcionSelect');
    
    select.innerHTML = '<option value="">Seleccionar...</option>';
    
    if (universo === '') {
        actualizarEstadoBotonAplicar();
        return;
    }
    
    let opcionesFiltradas;
    if (universo === 'AFIRMATIVOS') {
        opcionesFiltradas = opcionesVoto.filter(o => o.esAfirmativo);
    } else if (universo === 'EFECTIVOS') {
        opcionesFiltradas = opcionesVoto;
    } else if (universo === 'HABILITADOS') {
        opcionesFiltradas = [...opcionesVoto, { id: -1, nombre: 'Abstención' }];
    }
    
    if (opcionesFiltradas) {
        opcionesFiltradas.forEach(opcion => {
            const option = document.createElement('option');
            option.value = opcion.id;
            option.textContent = opcion.nombre;
            select.appendChild(option);
        });
    }
    
    actualizarEstadoBotonAplicar();
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
        actualizarTitulosMapas('Circuitos Electorales % de votos', 'Circuitos Electorales % NBI');
        actualizarAnalisisCorrelacion(circuitosData);
        renderizarTablaCircuitos(circuitosData);
        
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
    const checkboxes = document.querySelectorAll('#deptoCheckboxes input[type="checkbox"]');
    checkboxes.forEach(cb => cb.checked = false);
    
    // Resetear selects
    document.getElementById('universoSelect').value = '';
    document.getElementById('opcionSelect').innerHTML = '<option value="">Seleccionar...</option>';
    
    // Actualizar UI
    actualizarOpcionesVoto();
    actualizarEstadoBotonAplicar();
    
    // Recargar datos iniciales
    cargarDatosIniciales();
}

function actualizarTitulosMapas(tituloIzquierdo, tituloDerecho) {
    const tituloIzqEl = document.getElementById('tituloMapaIzquierdo');
    const tituloDerEl = document.getElementById('tituloMapaDerecho');
    if (tituloIzqEl) {
        tituloIzqEl.textContent = tituloIzquierdo;
    }
    if (tituloDerEl) {
        tituloDerEl.textContent = tituloDerecho;
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
    limpiarTablaCircuitos();
}

function renderizarTablaCircuitos(circuitosData) {
    const tablaBody = document.getElementById('tablaCircuitosBody');
    if (!tablaBody) return;

    const features = Array.isArray(circuitosData?.features) ? circuitosData.features : [];

    if (features.length === 0) {
        limpiarTablaCircuitos('Sin datos para los filtros seleccionados');
        return;
    }

    const filas = features
        .map(feature => feature?.properties || {})
        .sort((a, b) => {
            const deptoA = (a.departamentoNombre || '').toString();
            const deptoB = (b.departamentoNombre || '').toString();
            const compareDepto = deptoA.localeCompare(deptoB, 'es', { sensitivity: 'base' });
            if (compareDepto !== 0) return compareDepto;

            const codigoA = (a.codigo || '').toString();
            const codigoB = (b.codigo || '').toString();
            return codigoA.localeCompare(codigoB, 'es', { numeric: true, sensitivity: 'base' });
        });

    tablaBody.innerHTML = filas.map(props => {
        const departamento = props.departamentoNombre || 'Sin departamento';
        const circuito = props.codigo || props.id || '--';
        const porcentajeVoto = formatearPorcentaje(props.porcentajeVoto);
        const porcentajeNbi = formatearPorcentaje(props.porcentajeNbi);

        return `
            <tr>
                <td>${escapeHtml(departamento)}</td>
                <td>${escapeHtml(String(circuito))}</td>
                <td class="valor-numerico">${porcentajeVoto}</td>
                <td class="valor-numerico">${porcentajeNbi}</td>
            </tr>
        `;
    }).join('');
}

function limpiarTablaCircuitos(mensaje = 'Aplicá filtros para ver el detalle') {
    const tablaBody = document.getElementById('tablaCircuitosBody');
    if (!tablaBody) return;

    tablaBody.innerHTML = `
        <tr>
            <td colspan="4" class="tabla-vacia">${escapeHtml(mensaje)}</td>
        </tr>
    `;
}

function formatearPorcentaje(valor) {
    const numero = Number(valor);
    return Number.isFinite(numero) ? `${numero.toFixed(2)}%` : '--';
}

function escapeHtml(valor) {
    return String(valor)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function setLoading(loading) {
    const body = document.body;
    if (loading) {
        body.classList.add('loading');
    } else {
        body.classList.remove('loading');
    }
}
