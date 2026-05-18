/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence;

import iess.gen.FirmaIessApi.application.port.StoragePort;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <b>Adaptador de almacenamiento en MinIO (Object Storage).</b>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 * @version Revision: 2.0
 */
@Component
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
@RequiredArgsConstructor
public class MinioStorageAdapter implements StoragePort {

    private final MinioClient minioClient;

    @Value("${minio.bucket-default}")
    private String bucket;

    @Override
    public String almacenarBytes(byte[] bytes, String path, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType)
                    .build());
            return path;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir bytes a MinIO: " + path, e);
        }
    }

    @Override
    public String almacenar(MultipartFile archivo, String nombreUnico) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(nombreUnico)
                    .stream(archivo.getInputStream(), archivo.getSize(), -1)
                    .contentType(archivo.getContentType())
                    .build());
            return nombreUnico;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo a MinIO: " + nombreUnico, e);
        }
    }

    @Override
    public void eliminar(String nombreAlmacenado) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(nombreAlmacenado)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar archivo de MinIO: " + nombreAlmacenado, e);
        }
    }

    @Override
    public void eliminarObjeto(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar objeto de MinIO: " + path, e);
        }
    }

    @Override
    public List<String> listarObjetos(String prefijo) {
        List<String> paths = new ArrayList<>();
        try {
            Iterable<Result<Item>> resultados = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefijo)
                            .recursive(true)
                            .build());
            for (Result<Item> resultado : resultados) {
                paths.add(resultado.get().objectName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al listar objetos en MinIO con prefijo: " + prefijo, e);
        }
        return paths;
    }

    @Override
    public byte[] obtenerBytes(String path) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener bytes de MinIO: " + path, e);
        }
    }

    @Override
    public String generarUrlPresignada(String path, int horasExpiracion) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .method(Method.GET)
                            .expiry(horasExpiracion, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar URL presignada para: " + path, e);
        }
    }

    @Override
    public String getBucket() {
        return bucket;
    }
}
