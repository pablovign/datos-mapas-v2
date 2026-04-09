package com.psv.datos_mapas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psv.datos_mapas.model.RadioCensal;

import java.util.List;

@Repository
public interface RadioCensalRepository extends JpaRepository<RadioCensal, Integer> {
    List<RadioCensal> findByDepartamentoIdIn(List<Integer> departamentoIds);
}