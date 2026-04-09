package com.psv.datos_mapas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psv.datos_mapas.model.CircuitoElectoral;

import java.util.List;

@Repository
public interface CircuitoElectoralRepository extends JpaRepository<CircuitoElectoral, Integer> {
    List<CircuitoElectoral> findByDepartamentoIdIn(List<Integer> departamentoIds);
}