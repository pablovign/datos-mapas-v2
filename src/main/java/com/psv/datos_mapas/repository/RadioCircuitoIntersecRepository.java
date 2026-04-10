package com.psv.datos_mapas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.psv.datos_mapas.model.RadioCircuitoIntersec;

import java.util.List;

@Repository
public interface RadioCircuitoIntersecRepository extends JpaRepository<RadioCircuitoIntersec, Integer> {
    @EntityGraph(attributePaths = {"radioCensal"})
    List<RadioCircuitoIntersec> findByCircuitoElectoralIdIn(List<Integer> circuitoElectoralIds);
}
