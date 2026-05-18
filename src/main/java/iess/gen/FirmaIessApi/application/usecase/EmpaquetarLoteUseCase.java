/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.application.usecase;

import iess.gen.FirmaIessApi.application.port.StoragePort;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.EmpaquetarResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <b>Caso de uso que empaqueta los PDFs firmados de un lote en ZIPs de mÃ¡ximo 2GB
 * y los sube a MinIO eliminando los PDFs individuales ya empaquetados.</b>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 * @version Revision: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpaquetarLoteUseCase {

    /** TamaÃ±o mÃ¡ximo de cada ZIP en bytes (2GB). */
    private static final long MAX_ZIP_BYTES = 2L * 1024 * 1024 * 1024;

    private final StoragePort storagePort;

    /** Horas de expiraciÃ³n de las URLs presignadas. */
    @Value("${firmadigital.url-expiracion-horas:24}")
    private int urlExpiracionHoras;

    /** Nombre base del ZIP configurable desde el .env. */
    @Value("${firmadigital.zip-nombre:documentos_firmados_iess}")
    private String zipNombre;

    /**
     * <b>Empaqueta todos los PDFs firmados del lote en ZIPs de mÃ¡ximo 2GB.</b>
     *
     * @param idLote identificador del lote
     * @return respuesta con estado y totales
     */
    public EmpaquetarResponse empaquetar(String idLote) {
        try {
            // Construir prefijo de la carpeta del lote
            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String prefijo = fecha + "/temp_" + idLote + "/";

            // Listar todos los PDFs de la carpeta
            List<String> objetos = storagePort.listarObjetos(prefijo);
            List<String> pdfs = objetos.stream()
                    .filter(o -> o.endsWith("_signed.pdf"))
                    .toList();

            if (pdfs.isEmpty()) {
                return EmpaquetarResponse.builder()
                        .idLote(idLote)
                        .estado("ERROR")
                        .totalZips(0)
                        .totalDocumentos(0)
                        .mensaje("No se encontraron documentos firmados para el lote: " + idLote)
                        .build();
            }

            log.info("EmpaquetarLoteUseCase: empaquetando {} PDFs para lote {}", pdfs.size(), idLote);

            // Empaquetar en ZIPs de mÃ¡ximo 2GB
            int zipNumero = 1;
            int totalEmpaquetados = 0;
            long tamanoActual = 0;
            List<String> pdfsEnZipActual = new ArrayList<>();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (String pdfPath : pdfs) {
                byte[] pdfBytes = storagePort.obtenerBytes(pdfPath);

                // Si agregar este PDF supera el lÃ­mite, cerrar ZIP actual y crear uno nuevo
                if (tamanoActual + pdfBytes.length > MAX_ZIP_BYTES && !pdfsEnZipActual.isEmpty()) {
                    zos.close();
                    subirZip(baos.toByteArray(), prefijo, zipNumero, false);
                    eliminarPdfs(pdfsEnZipActual);
                    totalEmpaquetados += pdfsEnZipActual.size();
                    zipNumero++;
                    pdfsEnZipActual.clear();
                    baos = new ByteArrayOutputStream();
                    zos = new ZipOutputStream(baos);
                    tamanoActual = 0;
                }

                // Agregar PDF al ZIP actual
                String nombreArchivo = pdfPath.substring(pdfPath.lastIndexOf("/") + 1);
                zos.putNextEntry(new ZipEntry(nombreArchivo));
                zos.write(pdfBytes);
                zos.closeEntry();
                tamanoActual += pdfBytes.length;
                pdfsEnZipActual.add(pdfPath);
            }

            // Cerrar y subir el Ãºltimo ZIP
            if (!pdfsEnZipActual.isEmpty()) {
                zos.close();
                subirZip(baos.toByteArray(), prefijo, zipNumero, pdfs.size() <= pdfsEnZipActual.size() && zipNumero == 1);
                eliminarPdfs(pdfsEnZipActual);
                totalEmpaquetados += pdfsEnZipActual.size();
            }

            int totalZips = zipNumero;
            log.info("EmpaquetarLoteUseCase: {} PDFs empaquetados en {} ZIP(s) para lote {}",
                    totalEmpaquetados, totalZips, idLote);

            return EmpaquetarResponse.builder()
                    .idLote(idLote)
                    .estado("OK")
                    .totalZips(totalZips)
                    .totalDocumentos(totalEmpaquetados)
                    .mensaje(totalEmpaquetados + " documento(s) empaquetado(s) en " + totalZips + " ZIP(s).")
                    .build();

        } catch (Exception e) {
            log.error("EmpaquetarLoteUseCase: error al empaquetar lote {}: {}", idLote, e.getMessage());
            return EmpaquetarResponse.builder()
                    .idLote(idLote)
                    .estado("ERROR")
                    .totalZips(0)
                    .totalDocumentos(0)
                    .mensaje("Error al empaquetar: " + e.getMessage())
                    .build();
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // MÃ©todos privados
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Sube un ZIP a MinIO en la carpeta del lote.
     * Si esUnico=true usa el nombre sin nÃºmero, si no agrega el nÃºmero.
     */
    private void subirZip(byte[] zipBytes, String prefijo, int numero, boolean esUnico) {
        String nombreZip = esUnico
                ? zipNombre + ".zip"
                : zipNombre + "_" + numero + ".zip";
        String zipPath = prefijo + nombreZip;
        storagePort.almacenarBytes(zipBytes, zipPath, "application/zip");
        log.debug("EmpaquetarLoteUseCase: ZIP subido a MinIO: {}", zipPath);
    }

    /**
     * Elimina los PDFs individuales ya empaquetados de MinIO.
     */
    private void eliminarPdfs(List<String> paths) {
        for (String path : paths) {
            try {
                storagePort.eliminarObjeto(path);
                log.debug("EmpaquetarLoteUseCase: PDF eliminado de MinIO: {}", path);
            } catch (Exception e) {
                log.warn("EmpaquetarLoteUseCase: no se pudo eliminar PDF {}: {}", path, e.getMessage());
            }
        }
    }
}
