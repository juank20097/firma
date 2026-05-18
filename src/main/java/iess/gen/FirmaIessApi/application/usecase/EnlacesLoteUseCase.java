/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.application.usecase;

import iess.gen.FirmaIessApi.application.port.StoragePort;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.EnlacesResponse;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.EnlacesResponse.EnlaceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Caso de uso que genera los enlaces presignados de descarga
 * para los documentos firmados de un lote.</b>
 *
 * <p>LÃ³gica:</p>
 * <ul>
 *   <li>1 PDF â†’ link directo al PDF</li>
 *   <li>ZIPs â†’ link por cada ZIP</li>
 *   <li>MÃºltiples PDFs sin empaquetar â†’ error, debe empaquetar primero</li>
 *   <li>Carpeta vacÃ­a â†’ error</li>
 * </ul>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 * @version Revision: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnlacesLoteUseCase {

    private final StoragePort storagePort;

    @Value("${firmadigital.url-expiracion-horas:24}")
    private int urlExpiracionHoras;

    /**
     * Genera los enlaces de descarga para el lote especificado.
     *
     * @param idLote identificador del lote
     * @return respuesta con los enlaces presignados
     */
    public EnlacesResponse obtenerEnlaces(String idLote) {
        try {
            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String prefijo = fecha + "/temp_" + idLote + "/";

            List<String> objetos = storagePort.listarObjetos(prefijo);

            if (objetos.isEmpty()) {
                return EnlacesResponse.builder()
                        .idLote(idLote)
                        .estado("ERROR")
                        .mensaje("No se encontraron documentos para el lote: " + idLote
                                + ". Verifique el idLote o que la firma haya completado.")
                        .enlaces(List.of())
                        .build();
            }

            // Separar PDFs y ZIPs
            List<String> pdfs = objetos.stream()
                    .filter(o -> o.endsWith("_signed.pdf"))
                    .toList();
            List<String> zips = objetos.stream()
                    .filter(o -> o.endsWith(".zip"))
                    .toList();

            // Caso: mÃºltiples PDFs sin empaquetar
            if (pdfs.size() > 1 && zips.isEmpty()) {
                return EnlacesResponse.builder()
                        .idLote(idLote)
                        .estado("ERROR")
                        .mensaje("Hay " + pdfs.size() + " documentos sin empaquetar. "
                                + "Llame primero a POST /iess/firmaec/empaquetar/" + idLote)
                        .enlaces(List.of())
                        .build();
            }

            // Caso: 1 solo PDF â†’ link directo
            if (pdfs.size() == 1 && zips.isEmpty()) {
                String pdfPath = pdfs.get(0);
                String nombre = pdfPath.substring(pdfPath.lastIndexOf("/") + 1);
                String url = storagePort.generarUrlPresignada(pdfPath, urlExpiracionHoras);

                log.info("EnlacesLoteUseCase: link directo PDF generado para lote {}", idLote);
                return EnlacesResponse.builder()
                        .idLote(idLote)
                        .estado("OK")
                        .mensaje("1 documento disponible para descarga.")
                        .enlaces(List.of(EnlaceItem.builder()
                                .nombre(nombre)
                                .url(url)
                                .horasExpiracion(urlExpiracionHoras)
                                .build()))
                        .build();
            }

            // Caso: ZIPs generados
            List<EnlaceItem> enlaces = new ArrayList<>();
            for (String zipPath : zips) {
                String nombre = zipPath.substring(zipPath.lastIndexOf("/") + 1);
                String url = storagePort.generarUrlPresignada(zipPath, urlExpiracionHoras);
                enlaces.add(EnlaceItem.builder()
                        .nombre(nombre)
                        .url(url)
                        .horasExpiracion(urlExpiracionHoras)
                        .build());
            }

            log.info("EnlacesLoteUseCase: {} enlace(s) generado(s) para lote {}", enlaces.size(), idLote);
            return EnlacesResponse.builder()
                    .idLote(idLote)
                    .estado("OK")
                    .mensaje(enlaces.size() + " enlace(s) de descarga disponible(s).")
                    .enlaces(enlaces)
                    .build();

        } catch (Exception e) {
            log.error("EnlacesLoteUseCase: error al generar enlaces para lote {}: {}", idLote, e.getMessage());
            return EnlacesResponse.builder()
                    .idLote(idLote)
                    .estado("ERROR")
                    .mensaje("Error al generar enlaces: " + e.getMessage())
                    .enlaces(List.of())
                    .build();
        }
    }
}
