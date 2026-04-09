package com.psv.datos_mapas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psv.datos_mapas.model.OpcionVoto;

@Repository
public interface OpcionVotoRepository extends JpaRepository<OpcionVoto, Integer> {
}