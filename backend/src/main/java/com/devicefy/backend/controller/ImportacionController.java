package com.devicefy.backend.controller;

import com.devicefy.backend.dto.ActualizarDespliegueRequest;
import com.devicefy.backend.dto.AsignarCentrosRequest;
import com.devicefy.backend.dto.AsignarDesplieguesRequest;
import com.devicefy.backend.dto.CrearDespliegueRequest;
import com.devicefy.backend.dto.DespliegueEquipoResponse;
import com.devicefy.backend.dto.DespliegueResponse;
import com.devicefy.backend.dto.ImportacionResult;
import com.devicefy.backend.service.ImportacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/importaciones")
@RequiredArgsConstructor
public class ImportacionController {

    private final ImportacionService importacionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ImportacionResult importar(@RequestParam("file") MultipartFile file,
                                      @RequestParam("nombreDespliegue") String nombreDespliegue) {
        if (nombreDespliegue == null || nombreDespliegue.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del despliegue es obligatorio");
        }
        return importacionService.importar(nombreDespliegue.trim(), file);
    }

    @GetMapping("/despliegues")
    public List<DespliegueResponse> listarDespliegues() {
        return importacionService.listarDespliegues();
    }

    @PostMapping("/despliegues")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DespliegueResponse crearDespliegue(@Valid @RequestBody CrearDespliegueRequest request) {
        return importacionService.crear(request);
    }

    @GetMapping("/despliegues/{id}/equipos")
    public List<DespliegueEquipoResponse> listarEquipos(@PathVariable Long id) {
        return importacionService.listarEquipos(id);
    }

    @PutMapping("/despliegues/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DespliegueResponse actualizar(@PathVariable Long id,
                                         @Valid @RequestBody ActualizarDespliegueRequest request) {
        return importacionService.actualizar(id, request);
    }

    @DeleteMapping("/despliegues/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {
        importacionService.eliminar(id);
    }

    @PutMapping("/despliegues/{id}/tecnicos")
    @PreAuthorize("hasRole('ADMIN')")
    public DespliegueResponse asignarTecnicos(@PathVariable Long id,
                                              @RequestBody AsignarDesplieguesRequest request) {
        return importacionService.asignarTecnicos(id, request);
    }

    @PutMapping("/despliegues/{id}/centros")
    @PreAuthorize("hasRole('ADMIN')")
    public DespliegueResponse asignarCentros(@PathVariable Long id,
                                             @RequestBody AsignarCentrosRequest request) {
        return importacionService.asignarCentros(id, request);
    }
}
