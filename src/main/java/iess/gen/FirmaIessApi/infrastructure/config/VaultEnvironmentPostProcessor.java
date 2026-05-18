/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * <b> EnvironmentPostProcessor que carga los secretos de Vault
 * en la fase más temprana del arranque de Spring Boot,
 * antes de que cualquier bean sea construido.
 * Solo se activa cuando VAULT_ENABLED=true en el .env. </b>
 *
 * @author Juan Carlos Estévez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos Estévez Hidalgo , Date: 07 may 2026]
 *          </p>
 */
@Slf4j
public class VaultEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        String vaultEnabled = environment.getProperty("VAULT_ENABLED", "false");
        if (!"true".equalsIgnoreCase(vaultEnabled)) {
            return;
        }

        String vaultHost   = environment.getProperty("VAULT_HOST", "localhost");
        String vaultPort   = environment.getProperty("VAULT_PORT", "8200");
        String vaultScheme = environment.getProperty("VAULT_SCHEME", "http");
        String vaultToken  = environment.getProperty("VAULT_TOKEN", "root-token");
        String dbEngine    = environment.getProperty("DB_ENGINE", "postgres");

        log.info("Vault: cargando secretos en fase de bootstrap...");

        try {
            VaultEndpoint endpoint = VaultEndpoint.from(
                    new URI(vaultScheme + "://" + vaultHost + ":" + vaultPort));

            VaultTemplate vaultTemplate = new VaultTemplate(
                    endpoint, new TokenAuthentication(vaultToken));

            Map<String, Object> secretos = new HashMap<>();

            // ── PostgreSQL ─────────────────────────────────
            if ("postgres".equalsIgnoreCase(dbEngine)) {
                leerSecreto(vaultTemplate, "FirmaIessApi/data/database/postgres",
                        Map.of(
                            "host",     "DB_POSTGRES_HOST",
                            "port",     "DB_POSTGRES_PORT",
                            "username", "DB_POSTGRES_USERNAME",
                            "password", "DB_POSTGRES_PASSWORD",
                            "bdd",      "DB_POSTGRES_NAME"
                        ), secretos);

                String host = val(secretos, "DB_POSTGRES_HOST", "localhost");
                String port = val(secretos, "DB_POSTGRES_PORT", "5432");
                String bdd  = val(secretos, "DB_POSTGRES_NAME", "base_spring_db");
                String user = val(secretos, "DB_POSTGRES_USERNAME", "postgres");
                String pass = val(secretos, "DB_POSTGRES_PASSWORD", "postgres");
                secretos.put("spring.datasource.url",
                        "jdbc:postgresql://" + host + ":" + port + "/" + bdd);
                secretos.put("spring.datasource.username", user);
                secretos.put("spring.datasource.password", pass);
            }

            // ── Oracle ─────────────────────────────────────
            if ("oracle".equalsIgnoreCase(dbEngine)) {
                leerSecreto(vaultTemplate, "FirmaIessApi/data/database/oracle",
                        Map.of(
                            "host",     "DB_ORACLE_HOST",
                            "port",     "DB_ORACLE_PORT",
                            "username", "DB_ORACLE_USERNAME",
                            "password", "DB_ORACLE_PASSWORD",
                            "service",  "DB_ORACLE_SERVICE"
                        ), secretos);

                String host    = val(secretos, "DB_ORACLE_HOST", "localhost");
                String port    = val(secretos, "DB_ORACLE_PORT", "1521");
                String service = val(secretos, "DB_ORACLE_SERVICE", "ORCLPDB1");
                String user    = val(secretos, "DB_ORACLE_USERNAME", "oracle_user");
                String pass    = val(secretos, "DB_ORACLE_PASSWORD", "oracle_password");
                secretos.put("spring.datasource.url",
                        "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service);
                secretos.put("spring.datasource.username", user);
                secretos.put("spring.datasource.password", pass);
            }

            // ── MongoDB ────────────────────────────────────
            leerSecreto(vaultTemplate, "FirmaIessApi/data/database/mongo",
                    Map.of(
                        "host",     "DB_MONGO_HOST",
                        "port",     "DB_MONGO_PORT",
                        "bdd",      "DB_MONGO_NAME",
                        "username", "DB_MONGO_USERNAME",
                        "password", "DB_MONGO_PASSWORD",
                        "auth_db",  "DB_MONGO_AUTH_DB"
                    ), secretos);

            String mongoHost  = val(secretos, "DB_MONGO_HOST", "localhost");
            String mongoPort  = val(secretos, "DB_MONGO_PORT", "27017");
            String mongoUser  = val(secretos, "DB_MONGO_USERNAME", "mongo_user");
            String mongoPass  = val(secretos, "DB_MONGO_PASSWORD", "mongo_password");
            String mongoAuth  = val(secretos, "DB_MONGO_AUTH_DB", "admin");
            secretos.put("spring.data.mongodb.uri",
                    "mongodb://" + mongoUser + ":" + mongoPass +
                    "@" + mongoHost + ":" + mongoPort +
                    "/AUDITORIA_IESS?authSource=" + mongoAuth);

            // ── MinIO ──────────────────────────────────────
            leerSecreto(vaultTemplate, "FirmaIessApi/data/storage/minio",
                    Map.of(
                        "url",       "MINIO_URL",
                        "accessKey", "MINIO_ACCESS_KEY",
                        "secretKey", "MINIO_SECRET_KEY"
                    ), secretos);

            secretos.put("minio.url",
                    val(secretos, "MINIO_URL", "http://localhost:9000"));
            secretos.put("minio.access-key",
                    val(secretos, "MINIO_ACCESS_KEY", "minioadmin"));
            secretos.put("minio.secret-key",
                    val(secretos, "MINIO_SECRET_KEY", "minioadmin"));

            // Registrar con máxima prioridad en el Environment
            environment.getPropertySources().addFirst(
                    new MapPropertySource("vault-secrets", secretos));
            log.info("Vault: {} propiedades cargadas exitosamente.", secretos.size());

        } catch (Exception e) {
            log.warn("Vault: error al conectar. Usando valores del .env. Error: {}",
                    e.getMessage());
        }
    }

    /**
     * <b> Lee un path de Vault KV v2 y agrega los valores al mapa de secretos. </b>
     *
     * @param vaultTemplate cliente de Vault
     * @param path          path a leer
     * @param mapaClave     mapa de clave Vault → propiedad del sistema
     * @param secretos      mapa destino
     */
    @SuppressWarnings("unchecked")
    private void leerSecreto(VaultTemplate vaultTemplate, String path,
                              Map<String, String> mapaClave,
                              Map<String, Object> secretos) {
        try {
            VaultResponseSupport<Map> response = vaultTemplate.read(path, Map.class);
            if (response == null || response.getData() == null) {
                log.warn("Vault: path '{}' no encontrado.", path);
                return;
            }

            // KV v2 tiene doble anidamiento: data.data
            Object dataAnidado = response.getData().get("data");
            final Map<String, Object> valores = (dataAnidado instanceof Map)
                    ? (Map<String, Object>) dataAnidado
                    : response.getData();

            mapaClave.forEach((claveVault, propSistema) -> {
                Object valor = valores.get(claveVault);
                if (valor != null) {
                    secretos.put(propSistema, valor.toString());
                    log.info("Vault: [OK] {}/{} → {}", path, claveVault, propSistema);
                } else {
                    log.warn("Vault: clave '{}' no encontrada en '{}'.", claveVault, path);
                }
            });

        } catch (Exception e) {
            log.warn("Vault: error al leer '{}'. Error: {}", path, e.getMessage());
        }
    }

    /**
     * <b> Obtiene un valor del mapa o retorna el default si no existe o está vacío. </b>
     *
     * @param secretos mapa de secretos
     * @param clave    clave a buscar
     * @param defecto  valor por defecto
     * @return valor encontrado o defecto
     */
    private String val(Map<String, Object> secretos, String clave, String defecto) {
        Object v = secretos.get(clave);
        return (v != null && !v.toString().isBlank()) ? v.toString() : defecto;
    }
}
