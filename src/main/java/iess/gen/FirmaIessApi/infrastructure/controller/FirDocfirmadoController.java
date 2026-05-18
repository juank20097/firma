/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller;

import iess.gen.FirmaIessApi.application.usecase.EmpaquetarLoteUseCase;
import iess.gen.FirmaIessApi.application.usecase.EnlacesLoteUseCase;
import iess.gen.FirmaIessApi.application.usecase.FirDocfirmadoUseCase;
import iess.gen.FirmaIessApi.application.usecase.FirLoteUseCase;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.EmpaquetarResponse;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.EnlacesResponse;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.FirmarLoteRequest;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.FirmarLoteResponse;
import iess.gen.FirmaIessApi.model.FirDocfirmado;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/iess/firmaec")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FirDocfirmadoController {

    private final FirDocfirmadoUseCase useCase;
    private final FirLoteUseCase loteUseCase;
    private final EmpaquetarLoteUseCase empaquetarUseCase;
    private final EnlacesLoteUseCase enlacesUseCase;

    /**
     * Callback de firmadigital-servicio â€” recibe el documento firmado y lo persiste.
     */
    @PostMapping
    public String insertar(@RequestBody FirDocfirmado request) {
        useCase.crear(request);
        return "OK";
    }

    /**
     * WS integrador â€” orquesta el flujo completo de firma digital en lote.
     */
    @PostMapping("/firmar")
    public FirmarLoteResponse firmar(@RequestBody FirmarLoteRequest request) {
        return loteUseCase.firmar(request);
    }

    /**
     * Empaqueta los PDFs firmados de un lote en ZIPs de mÃ¡ximo 2GB en MinIO.
     */
    @PostMapping("/empaquetar/{idLote}")
    public EmpaquetarResponse empaquetar(@PathVariable String idLote) {
        return empaquetarUseCase.empaquetar(idLote);
    }

    /**
     * Devuelve los enlaces presignados de descarga para los documentos firmados del lote.
     * Si hay 1 PDF devuelve el link directo. Si hay ZIPs devuelve un link por ZIP.
     *
     * @param idLote identificador del lote
     * @return enlaces de descarga con fecha de expiraciÃ³n
     */
    @GetMapping("/enlaces/{idLote}")
    public EnlacesResponse obtenerEnlaces(@PathVariable String idLote) {
        return enlacesUseCase.obtenerEnlaces(idLote);
    }
}
