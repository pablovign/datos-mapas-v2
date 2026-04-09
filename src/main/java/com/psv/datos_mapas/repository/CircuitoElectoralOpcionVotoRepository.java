package com.psv.datos_mapas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psv.datos_mapas.model.CircuitoElectoralOpcionVoto;

import java.util.List;

@Repository
public interface CircuitoElectoralOpcionVotoRepository extends JpaRepository<CircuitoElectoralOpcionVoto, Integer> {
    List<CircuitoElectoralOpcionVoto> findByCircuitoElectoralId(Integer circuitoId);
}