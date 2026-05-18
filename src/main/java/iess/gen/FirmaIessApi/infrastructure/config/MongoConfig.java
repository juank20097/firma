/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * <b> Configuración condicional de MongoDB para auditoría.
 * Solo se activa cuando MONGO_ENABLED=true en el .env.
 * Si MONGO_ENABLED=false (default), Spring no intenta conectarse
 * a MongoDB y el servicio levanta sin el motor disponible.
 * Al iniciar crea automáticamente la base de datos AUDITORIA_IESS
 * y las colecciones necesarias si no existen. </b>
 *
 * @author Juan Carlos Estévez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos Estévez Hidalgo , Date: 07 may 2026]
 *          </p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "MONGO_ENABLED", havingValue = "true")
@EnableMongoRepositories(basePackages = "iess.gen.FirmaIessApi.infrastructure.persistence.mongo")
public class MongoConfig {

    /** Nombre fijo de la base de datos de auditoría. */
    private static final String AUDITORIA_DB = "AUDITORIA_IESS";

    /** URI de conexión a MongoDB. */
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * <b> Crea el cliente MongoDB con la URI configurada en el .env. </b>
     *
     * @return instancia configurada de MongoClient
     */
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * <b> Crea el factory de la base de datos de auditoría. </b>
     *
     * @return MongoDatabaseFactory apuntando a AUDITORIA_IESS
     */
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), AUDITORIA_DB);
    }

    /**
     * <b> Crea el MongoTemplate y verifica que la base de datos AUDITORIA_IESS
     * exista. MongoDB la crea automáticamente al primer insert,
     * pero este bean confirma la conectividad al arrancar. </b>
     *
     * @return MongoTemplate configurado para AUDITORIA_IESS
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate template = new MongoTemplate(mongoDatabaseFactory());
        verificarConexion(template);
        return template;
    }

    /**
     * <b> Verifica la conexión a MongoDB al arrancar.
     * MongoDB crea la base de datos automáticamente al primer insert,
     * no es necesario crearla explícitamente. </b>
     *
     * @param template MongoTemplate para ejecutar el ping
     */
    private void verificarConexion(MongoTemplate template) {
        try {
            template.getDb().runCommand(new org.bson.Document("ping", 1));
            log.info("MongoDB: conexión exitosa a la base de datos '{}'.", AUDITORIA_DB);
            log.info("MongoDB: la base de datos se creará automáticamente al primer registro de auditoría.");
        } catch (Exception e) {
            log.warn("MongoDB: no se pudo conectar a '{}'. Error: {}", AUDITORIA_DB, e.getMessage());
        }
    }
}
