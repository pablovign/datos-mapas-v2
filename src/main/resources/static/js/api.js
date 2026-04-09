const API_BASE = '/api/v1';

async function fetchDepartamentos() {
    const response = await fetch(`${API_BASE}/departamentos`);
    if (!response.ok) throw new Error('Error cargando departamentos');
    return response.json();
}

async function fetchCircuitosVotos(departamentoIds, universo, opcionVotoId) {
    const params = new URLSearchParams();
    
    if (departamentoIds && departamentoIds.length > 0) {
        params.append('departamentoIds', departamentoIds.join(','));
    }
    if (universo) {
        params.append('universo', universo);
    }
    if (opcionVotoId !== null && opcionVotoId !== undefined && opcionVotoId !== '') {
        params.append('opcionVotoId', opcionVotoId);
    }
    
    const url = `${API_BASE}/circuitos/votos${params.toString() ? '?' + params.toString() : ''}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Error cargando circuitos');
    return response.json();
}

async function fetchRadiosNbi(departamentoIds) {
    const params = new URLSearchParams();
    
    if (departamentoIds && departamentoIds.length > 0) {
        params.append('departamentoIds', departamentoIds.join(','));
    }
    
    const url = `${API_BASE}/radios/nbi${params.toString() ? '?' + params.toString() : ''}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Error cargando radios');
    return response.json();
}

async function fetchOpcionesVoto() {
    const response = await fetch(`${API_BASE}/opciones-voto`);
    if (!response.ok) throw new Error('Error cargando opciones de voto');
    return response.json();
}
