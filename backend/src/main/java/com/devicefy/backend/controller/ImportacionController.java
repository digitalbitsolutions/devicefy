package com.devicefy.backend.controller;

import com.devicefy.backend.dto.DespliegueEquipoResponse;
import com.devicefy.backend.dto.DespliegueResponse;
import com.devicefy.backend.dto.ImportacionResult;
import com.devicefy.backend.service.ImportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/despliegues/{id}/equipos")
    public List<DespliegueEquipoResponse> listarEquipos(@PathVariable Long id) {
        return importacionService.listarEquipos(id);
    }
}
