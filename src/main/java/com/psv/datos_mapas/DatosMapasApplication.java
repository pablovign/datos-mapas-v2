package com.psv.datos_mapas;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatosMapasApplication {

	public static void main(String[] args) {
		// Carga el archivo .env antes de arrancar Spring
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(DatosMapasApplication.class, args);
	}

}
