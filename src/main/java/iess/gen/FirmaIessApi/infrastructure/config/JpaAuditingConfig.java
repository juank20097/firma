/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * <b> Configuración de auditoría automática para JPA.
 * Habilita el llenado automático de los campos fecha_creacion, fecha_actualizacion,
 * creado_por y actualizado_por en cada operación INSERT y UPDATE. </b>
 *
 * @author Juan Carlos Estévez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos Estévez Hidalgo , Date: 07 may 2026]
 *          </p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * <b> Bean RestTemplate con timeouts configurados para llamadas a firmadigital. </b>
     *
     * @return instancia de RestTemplate con 60s de timeout
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60_000);  // 60 segundos para conectar
        factory.setReadTimeout(120_000);    // 120 segundos para leer respuesta
        return new RestTemplate(factory);
    }

    /**
     * <b> Provee el usuario actual para los campos de auditoría. </b>
     * Cuando se integre Spring Security, reemplazar el valor estático "system"
     * por SecurityContextHolder.getContext().getAuthentication().getName(). </b>
     *
     * @return AuditorAware con el nombre del usuario actual
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
