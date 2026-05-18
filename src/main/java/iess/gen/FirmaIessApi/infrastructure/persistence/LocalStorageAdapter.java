/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence;

import iess.gen.FirmaIessApi.application.port.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * <b> Adaptador de almacenamiento local en disco.
 * Implementa el puerto StoragePort y se activa cuando MINIO_ENABLED=false (valor por defecto).
 * Los archivos se guardan en el directorio configurado por STORAGE_LOCAL_PATH. </b>
 *
 * @author Juan Carlos Estévez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos Estévez Hidalgo , Date: 07 may 2026]
 *          </p>
 */
@Component
@ConditionalOnProperty(name = "minio.enabled", havingValue = "false", matchIfMissing = true)
public class LocalStorageAdapter implements StoragePort {

    /** Ruta del directorio local donde se almacenan los archivos. */
    @Value("${storage.local.path:uploads}")
    private String uploadPath;

    /**
     * <b> Almacena el archivo en el directorio local configurado. </b>
     *
     * @param archivo      archivo multipart recibido desde el controller
     * @param nombreUnico  nombre único generado (UUID + extensión)
     * @return ruta absoluta del archivo almacenado
     * @throws RuntimeException si ocurre un error de I/O al guardar el archivo
     */
    @Override
    public String almacenar(MultipartFile archivo, String nombreUnico) {
        try {
            Path directorio = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(directorio);
            Path destino = directorio.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return destino.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al almacenar el archivo localmente: " + nombreUnico, e);
        }
    }

    /**
     * <b> Elimina el archivo del directorio local. </b>
     *
     * @param nombreAlmacenado nombre único del archivo a eliminar
     * @throws RuntimeException si ocurre un error de I/O al eliminar el archivo
     */
    @Override
    public void eliminar(String nombreAlmacenado) {
        try {
            Path archivo = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(nombreAlmacenado);
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo: " + nombreAlmacenado, e);
        }
    }

    /**
     * <b> No implementado en almacenamiento local.
     * Lanza UnsupportedOperationException si se invoca. </b>
     *
     * @param bytes       contenido del archivo en bytes
     * @param path        ruta del archivo
     * @param contentType tipo MIME
     * @return nunca retorna
     */
    @Override
    public String almacenarBytes(byte[] bytes, String path, String contentType) {
        throw new UnsupportedOperationException("almacenarBytes no está soportado en almacenamiento local.");
    }

    @Override
    public void eliminarObjeto(String path) {
        throw new UnsupportedOperationException("eliminarObjeto no está soportado en almacenamiento local.");
    }

    @Override
    public java.util.List<String> listarObjetos(String prefijo) {
        throw new UnsupportedOperationException("listarObjetos no está soportado en almacenamiento local.");
    }

    @Override
    public byte[] obtenerBytes(String path) {
        throw new UnsupportedOperationException("obtenerBytes no está soportado en almacenamiento local.");
    }

    @Override
    public String generarUrlPresignada(String path, int horasExpiracion) {
        throw new UnsupportedOperationException("generarUrlPresignada no está soportado en almacenamiento local.");
    }

    @Override
    public String getBucket() {
        return null;
    }
}
