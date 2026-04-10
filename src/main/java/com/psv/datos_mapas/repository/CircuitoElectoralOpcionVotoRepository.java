package com.psv.datos_mapas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.psv.datos_mapas.model.CircuitoElectoralOpcionVoto;

import java.util.List;

@Repository
public interface CircuitoElectoralOpcionVotoRepository extends JpaRepository<CircuitoElectoralOpcionVoto, Integer> {
    @EntityGraph(attributePaths = {"opcionVoto"})
    List<CircuitoElectoralOpcionVoto> findByCircuitoElectoralIdIn(List<Integer> circuitoIds);
}
