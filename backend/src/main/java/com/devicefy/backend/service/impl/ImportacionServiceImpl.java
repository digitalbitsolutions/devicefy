package com.devicefy.backend.service.impl;

import com.devicefy.backend.domain.Centro;
import com.devicefy.backend.domain.Despliegue;
import com.devicefy.backend.domain.DespliegueEquipo;
import com.devicefy.backend.domain.Entidad;
import com.devicefy.backend.domain.Equipo;
import com.devicefy.backend.domain.RedConfig;
import com.devicefy.backend.domain.Ubicacion;
import com.devicefy.backend.domain.Usuario;
import com.devicefy.backend.domain.enums.RolNombre;
import com.devicefy.backend.domain.enums.TipoAsignacionRed;
import com.devicefy.backend.dto.ActualizarDespliegueRequest;
import com.devicefy.backend.dto.AsignarCentrosRequest;
import com.devicefy.backend.dto.AsignarDesplieguesRequest;
import com.devicefy.backend.dto.CrearDespliegueRequest;
import com.devicefy.backend.dto.DespliegueEquipoResponse;
import com.devicefy.backend.dto.DespliegueResponse;
import com.devicefy.backend.dto.ErrorImportacion;
import com.devicefy.backend.dto.ImportacionResult;
import com.devicefy.backend.repository.CentroRepository;
import com.devicefy.backend.repository.DespliegueEquipoRepository;
import com.devicefy.backend.repository.DespliegueRepository;
import com.devicefy.backend.repository.EntidadRepository;
import com.devicefy.backend.repository.EquipoRepository;
import com.devicefy.backend.repository.RedConfigRepository;
import com.devicefy.backend.repository.UbicacionRepository;
import com.devicefy.backend.service.ImportacionService;
import com.devicefy.backend.service.OllamaColumnMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImportacionServiceImpl implements ImportacionService {

    private static final Pattern PATRON_ID = Pattern.compile("^(.*?)\\s*\\(\\s*(\\d+)\\s*\\)\\s*$");
    private static final Pattern PATRON_IPV4 = Pattern.compile("\\b\\d{1,3}(?:\\.\\d{1,3}){3}\\b");
    private static final Pattern PATRON_RENOVE = Pattern.compile("^R\\s*(\\d{4})$");
    private static final Pattern PATRON_PLANTA = Pattern.compile("(?i).*planta.*");
    private static final Set<String> ESTADOS_PROYECTO = Set.of("PENDIENTE", "EN_PROCESO", "FINALIZADO");

    private final DespliegueRepository despliegueRepository;
    private final DespliegueEquipoRepository despliegueEquipoRepository;
    private final EntidadRepository entidadRepository;
    private final CentroRepository centroRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EquipoRepository equipoRepository;
    private final RedConfigRepository redConfigRepository;
    private final com.devicefy.backend.repository.UsuarioRepository usuarioRepository;
    private final OllamaColumnMapper ollamaColumnMapper;

    @Override
    @Transactional
    public ImportacionResult importar(String nombreDespliegue, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe subir un archivo Excel");
        }
        try (Workbook wb = WorkbookFactory.create(archivo.getInputStream())) {
            return importarLibro(nombreDespliegue, archivo.getOriginalFilename(), wb);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo Excel");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespliegueResponse> listarDespliegues() {
        return despliegueRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDespliegueResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespliegueEquipoResponse> listarEquipos(Long despliegueId) {
        return despliegueEquipoRepository.findByDespliegueIdOrderByHostnameActualAsc(despliegueId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DespliegueResponse crear(CrearDespliegueRequest request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del proyecto es obligatorio");
        }
        if (despliegueRepository.findByNombre(request.getNombre().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe un proyecto con el nombre " + request.getNombre().trim());
        }
        Despliegue nuevo = new Despliegue();
        nuevo.setNombre(request.getNombre().trim());
        nuevo.setProvincia(request.getProvincia());
        nuevo.setComunidadAutonoma(request.getComunidadAutonoma());
        nuevo.setEstado(validarEstadoProyecto(request.getEstado(), "PENDIENTE"));
        if (request.getCentroIds() != null) {
            for (Long cid : request.getCentroIds()) {
                centroRepository.findById(cid).ifPresent(nuevo.getCentros()::add);
            }
        }
        aplicarTecnicos(nuevo, request.getTecnicoIds());
        return toDespliegueResponse(despliegueRepository.save(nuevo));
    }

    @Override
    @Transactional
    public DespliegueResponse actualizar(Long despliegueId, ActualizarDespliegueRequest request) {
        Despliegue d = buscar(despliegueId);
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            despliegueRepository.findByNombre(request.getNombre().trim())
                    .filter(existente -> !existente.getId().equals(despliegueId))
                    .ifPresent(existente -> {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ya existe un proyecto con el nombre " + request.getNombre().trim());
                    });
            d.setNombre(request.getNombre().trim());
        }
        if (request.getProvincia() != null) {
            d.setProvincia(request.getProvincia().isBlank() ? null : request.getProvincia().trim());
        }
        if (request.getComunidadAutonoma() != null) {
            d.setComunidadAutonoma(request.getComunidadAutonoma().isBlank()
                    ? null : request.getComunidadAutonoma().trim());
        }
        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            d.setEstado(validarEstadoProyecto(request.getEstado(), d.getEstado()));
        }
        if (request.getCentroIds() != null) {
            d.getCentros().clear();
            for (Long centroId : request.getCentroIds()) {
                centroRepository.findById(centroId).ifPresent(d.getCentros()::add);
            }
        }
        if (request.getTecnicoIds() != null) {
            aplicarTecnicos(d, request.getTecnicoIds());
        }
        return toDespliegueResponse(despliegueRepository.save(d));
    }

    @Override
    @Transactional
    public void eliminar(Long despliegueId) {
        Despliegue d = buscar(despliegueId);
        despliegueRepository.delete(d);
    }

    @Override
    @Transactional
    public DespliegueResponse asignarTecnicos(Long despliegueId, AsignarDesplieguesRequest request) {
        Despliegue d = buscar(despliegueId);
        aplicarTecnicos(d, request.getDespliegueIds());
        return toDespliegueResponse(despliegueRepository.save(d));
    }

    private void aplicarTecnicos(Despliegue despliegue, List<Long> tecnicoIds) {
        despliegue.getTecnicos().clear();
        if (tecnicoIds == null) {
            return;
        }
        for (Long tecnicoId : tecnicoIds) {
            Usuario tecnico = usuarioRepository.findById(tecnicoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Técnico no encontrado: " + tecnicoId));
            boolean tieneRolTecnico = tecnico.getRoles().stream()
                    .anyMatch(rol -> rol.getNombre() == RolNombre.TECNICO);
            if (!tieneRolTecnico) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "El usuario no tiene rol de técnico: " + tecnico.getUsername());
            }
            despliegue.getTecnicos().add(tecnico);
        }
    }

    private String validarEstadoProyecto(String estado, String valorPorDefecto) {
        if (estado == null || estado.isBlank()) {
            return valorPorDefecto;
        }
        String normalizado = estado.trim().toUpperCase(java.util.Locale.ROOT);
        if (!ESTADOS_PROYECTO.contains(normalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de proyecto no válido: " + estado);
        }
        return normalizado;
    }

    @Override
    @Transactional
    public DespliegueResponse asignarCentros(Long despliegueId, AsignarCentrosRequest request) {
        Despliegue d = buscar(despliegueId);
        d.getCentros().clear();
        if (request.getCentroIds() != null) {
            for (Long cid : request.getCentroIds()) {
                centroRepository.findById(cid).ifPresent(d.getCentros()::add);
            }
        }
        return toDespliegueResponse(despliegueRepository.save(d));
    }

    private Despliegue buscar(Long id) {
        return despliegueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado"));
    }

    private DespliegueResponse toDespliegueResponse(Despliegue d) {
        return new DespliegueResponse(d.getId(), d.getNombre(), d.getProvincia(), d.getComunidadAutonoma(),
                d.getFicheroNombre(), d.getFechaImportacion(), d.getEstado(),
                despliegueEquipoRepository.countByDespliegueId(d.getId()),
                despliegueEquipoRepository.countByDespliegueIdAndEstado(d.getId(), "EN_PROCESO"),
                despliegueEquipoRepository.countByDespliegueIdAndEstado(d.getId(), "HECHO"),
                d.getTecnicos().stream().map(u -> u.getId()).sorted().toList(),
                d.getTecnicos().stream().map(u -> u.getNombreCompleto()).sorted().toList(),
                d.getCentros().stream().map(c -> c.getId()).sorted().toList(),
                d.getCentros().stream().map(c -> c.getNombre()).sorted().toList());
    }

    private ImportacionResult importarLibro(String nombreDespliegue, String nombreFichero, Workbook wb) {
        if (wb.getNumberOfSheets() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no contiene hojas");
        }
        Sheet hoja = wb.getSheetAt(0);
        if (!esFormatoTabular(hoja)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Formato no reconocido. Se soportan los formatos tabulares (Relació / CAPs / Terres de l'Ebre)");
        }
        DataFormatter df = new DataFormatter();
        List<String> cabeceras = leerCabeceras(hoja, df);
        List<List<String>> ejemplos = leerEjemplos(hoja, df, cabeceras.size());
        Map<String, Integer> columnasIA = ollamaColumnMapper.mapear(cabeceras, ejemplos);
        Map<String, Integer> columnas = mapearColumnas(hoja.getRow(0));
        if (!columnasIA.isEmpty()) {
            columnas.putAll(columnasIA);
        }
        if (columnas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no tiene cabeceras reconocidas");
        }
        String perfilMaqueta = extraerPerfilMaqueta(hoja.getRow(0), columnas);

        List<FilaImport> filas = leerFilas(hoja, columnas, perfilMaqueta, df);
        List<ErrorImportacion> errores = validar(filas);
        Set<Integer> filasConError = errores.stream().map(ErrorImportacion::fila).collect(java.util.stream.Collectors.toSet());

        Despliegue despliegue = despliegueRepository.findByNombre(nombreDespliegue)
                .orElseGet(() -> {
                    Despliegue nuevo = new Despliegue();
                    nuevo.setNombre(nombreDespliegue);
                    nuevo.setFicheroNombre(nombreFichero);
                    nuevo.setEstado("PENDIENTE");
                    return despliegueRepository.save(nuevo);
                });
        despliegue.setFechaImportacion(Instant.now());
        despliegue.setFicheroNombre(nombreFichero);

        Contadores c = new Contadores();
        for (FilaImport f : filas) {
            if (!filasConError.contains(f.fila)) {
                persistir(f, despliegue, c);
            }
        }
        return new ImportacionResult(despliegue.getId(), nombreDespliegue, "TABULAR",
                filas.size(), c.equipos, c.centros, c.ubicaciones, errores.size(), errores);
    }

    private List<String> leerCabeceras(Sheet hoja, DataFormatter df) {
        List<String> cabeceras = new ArrayList<>();
        Row fila = hoja.getRow(0);
        if (fila == null) {
            return cabeceras;
        }
        int max = Math.max(0, fila.getLastCellNum());
        for (int i = 0; i < max; i++) {
            String v = celda(hoja, 0, i, df);
            cabeceras.add(v.isBlank() ? "columna" + i : v);
        }
        return cabeceras;
    }

    private List<List<String>> leerEjemplos(Sheet hoja, DataFormatter df, int numColumnas) {
        List<List<String>> ejemplos = new ArrayList<>();
        int ultima = hoja.getLastRowNum();
        for (int r = 1; r <= Math.min(3, ultima); r++) {
            List<String> fila = new ArrayList<>();
            for (int i = 0; i < numColumnas; i++) {
                fila.add(celda(hoja, r, i, df));
            }
            ejemplos.add(fila);
        }
        return ejemplos;
    }

    private boolean esFormatoTabular(Sheet hoja) {
        Row fila = hoja.getRow(0);
        if (fila == null) {
            return false;
        }
        Set<String> claves = new HashSet<>();
        for (Cell celda : fila) {
            String k = normalizar(celda.toString());
            if (!k.isEmpty()) {
                claves.add(k);
            }
        }
        return claves.contains("nom") || claves.contains("entitat") || claves.contains("ubicacio")
                || claves.contains("maqueta") || claves.contains("fabricant");
    }

    private Map<String, Integer> mapearColumnas(Row fila) {
        Map<String, Integer> columnas = new HashMap<>();
        if (fila == null) {
            return columnas;
        }
        for (Cell celda : fila) {
            int idx = celda.getColumnIndex();
            String k = normalizar(celda.toString());
            if (k.isEmpty()) {
                continue;
            }
            switch (k) {
                case "etiqueta" -> columnas.putIfAbsent("etiqueta", idx);
                case "nom" -> columnas.putIfAbsent("nom", idx);
                case "entitat" -> columnas.putIfAbsent("entitat", idx);
                case "fabricant" -> columnas.putIfAbsent("fabricante", idx);
                case "numerodeserie" -> columnas.putIfAbsent("serie", idx);
                case "model" -> columnas.putIfAbsent("modelo", idx);
                case "ubicacio" -> columnas.putIfAbsent("ubicacion", idx);
                case "estat" -> columnas.putIfAbsent("estado", idx);
                case "rhe" -> columnas.putIfAbsent("rhe", idx);
                case "datadecreacio" -> columnas.putIfAbsent("fecha", idx);
                default -> {
                    if (k.startsWith("sistemaoperatiu")) {
                        String parte = k.substring("sistemaoperatiu".length());
                        if ("nom".equals(parte)) columnas.putIfAbsent("so_nom", idx);
                        else if ("versio".equals(parte)) columnas.putIfAbsent("so_version", idx);
                        else if ("servicepack".equals(parte)) columnas.putIfAbsent("so_sp", idx);
                    } else if (k.startsWith("components")) {
                        columnas.putIfAbsent("procesador", idx);
                    } else if (k.startsWith("suport")) {
                        columnas.putIfAbsent("incidencias", idx);
                    } else if (k.startsWith("xarxa")) {
                        columnas.putIfAbsent("ip", idx);
                    } else if (k.startsWith("maqueta")) {
                        columnas.putIfAbsent("maqueta", idx);
                    } else if (k.startsWith("etiqueta")) {
                        columnas.putIfAbsent("etiqueta", idx);
                    } else if (k.contains("serie")) {
                        columnas.putIfAbsent("serie", idx);
                    } else if (k.startsWith("nombre") || k.startsWith("equipo") || k.startsWith("hostname")) {
                        columnas.putIfAbsent("nom", idx);
                    }
                }
            }
        }
        return columnas;
    }

    private String extraerPerfilMaqueta(Row fila, Map<String, Integer> columnas) {
        Integer idx = columnas.get("maqueta");
        if (idx == null || fila == null) {
            return null;
        }
        Cell celda = fila.getCell(idx);
        if (celda == null) {
            return null;
        }
        Matcher m = Pattern.compile("(?i)maqueta\\s*(.*)").matcher(celda.toString());
        String perfil = m.find() ? m.group(1).trim() : "";
        return perfil.isEmpty() ? null : perfil;
    }

    private List<FilaImport> leerFilas(Sheet hoja, Map<String, Integer> columnas, String perfilMaqueta,
                                       DataFormatter df) {
        List<FilaImport> filas = new ArrayList<>();
        int ultima = hoja.getLastRowNum();
        for (int r = 1; r <= ultima; r++) {
            String nom = celda(hoja, r, columnas.get("nom"), df);
            String serie = celda(hoja, r, columnas.get("serie"), df);
            String fabricante = celda(hoja, r, columnas.get("fabricante"), df);
            String ip = celda(hoja, r, columnas.get("ip"), df);
            if (nom.isBlank() && serie.isBlank() && fabricante.isBlank() && ip.isBlank()) {
                continue;
            }
            FilaImport f = new FilaImport();
            f.fila = r + 1;
            f.hostname = parsearHostname(nom);
            f.idInventario = parsearIdInventario(nom);
            f.etiqueta = celda(hoja, r, columnas.get("etiqueta"), df);
            f.entidad = celda(hoja, r, columnas.get("entitat"), df);
            f.fabricante = fabricante;
            f.serie = serie;
            f.modelo = celda(hoja, r, columnas.get("modelo"), df);
            f.sistemaOperativo = unirSo(
                    celda(hoja, r, columnas.get("so_nom"), df),
                    celda(hoja, r, columnas.get("so_version"), df),
                    celda(hoja, r, columnas.get("so_sp"), df));
            f.ubicacion = celda(hoja, r, columnas.get("ubicacion"), df);
            f.procesador = celda(hoja, r, columnas.get("procesador"), df);
            f.incidencias = celda(hoja, r, columnas.get("incidencias"), df);
            f.estadoOrigen = celda(hoja, r, columnas.get("estado"), df);
            f.fechaCreacion = celda(hoja, r, columnas.get("fecha"), df);
            List<String> ipv4 = extraerIpv4(ip);
            if (!ipv4.isEmpty()) {
                f.ip = ipv4.get(0);
                f.ipsSecundarias = ipv4.subList(1, ipv4.size());
            }
            String rhe = celda(hoja, r, columnas.get("rhe"), df);
            parsearRenove(f, rhe);
            String maqueta = celda(hoja, r, columnas.get("maqueta"), df);
            f.perfilImagen = perfilMaqueta != null ? perfilMaqueta : (maqueta.isBlank() ? null : maqueta);
            filas.add(f);
        }
        return filas;
    }

    private String parsearHostname(String nom) {
        Matcher m = PATRON_ID.matcher(nom);
        return m.matches() ? m.group(1).trim() : nom.trim();
    }

    private String parsearIdInventario(String nom) {
        Matcher m = PATRON_ID.matcher(nom);
        return m.matches() ? m.group(2) : null;
    }

    private String unirSo(String nom, String version, String sp) {
        StringBuilder sb = new StringBuilder();
        if (!nom.isBlank()) {
            sb.append(nom);
        }
        if (!version.isBlank()) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(version);
        }
        if (!sp.isBlank()) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(sp);
        }
        return sb.toString();
    }

    private List<String> extraerIpv4(String texto) {
        List<String> resultado = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return resultado;
        }
        Matcher m = PATRON_IPV4.matcher(texto);
        while (m.find()) {
            resultado.add(m.group());
        }
        return resultado;
    }

    private void parsearRenove(FilaImport f, String rhe) {
        if (rhe == null || rhe.isBlank()) {
            return;
        }
        String valor = rhe.trim();
        Matcher m = PATRON_RENOVE.matcher(valor);
        if (m.matches()) {
            f.estadoRenove = "R";
            f.anioRenove = Integer.parseInt(m.group(1));
        } else if ("R".equalsIgnoreCase(valor)) {
            f.estadoRenove = "R";
        } else if ("E".equalsIgnoreCase(valor)) {
            f.estadoRenove = "E";
        } else if ("H".equalsIgnoreCase(valor)) {
            f.estadoRenove = "H";
        }
    }

    private List<ErrorImportacion> validar(List<FilaImport> filas) {
        List<ErrorImportacion> errores = new ArrayList<>();
        Set<String> hosts = new HashSet<>();
        Set<String> series = new HashSet<>();
        Set<String> etiquetas = new HashSet<>();
        Set<String> ips = new HashSet<>();
        for (FilaImport f : filas) {
            String motivo = null;
            if (f.hostname == null || f.hostname.isBlank()) {
                motivo = "Hostname vacío";
            } else if (!hosts.add(normalizar(f.hostname))) {
                motivo = "Hostname duplicado en el fichero";
            } else if (equipoRepository.existsByHostname(f.hostname)) {
                motivo = "Hostname ya existe en el sistema";
            }
            if (motivo == null && f.serie != null && !f.serie.isBlank()) {
                if (!series.add(normalizar(f.serie))) {
                    motivo = "Nº de serie duplicado en el fichero";
                } else if (equipoRepository.existsByNumeroSerie(f.serie)) {
                    motivo = "Nº de serie ya existe en el sistema";
                }
            }
            if (motivo == null && f.etiqueta != null && !f.etiqueta.isBlank()) {
                if (!etiquetas.add(normalizar(f.etiqueta))) {
                    motivo = "Etiqueta patrimonial duplicada en el fichero";
                } else if (equipoRepository.existsByEtiquetaPatrimonial(f.etiqueta)) {
                    motivo = "Etiqueta patrimonial ya existe en el sistema";
                }
            }
            if (motivo == null && f.ip != null && !f.ip.isBlank()) {
                if (!ips.add(normalizar(f.ip))) {
                    motivo = "IP duplicada en el fichero";
                } else if (redConfigRepository.existsByIp(f.ip)) {
                    motivo = "IP ya existe en el sistema";
                }
            }
            if (motivo != null) {
                errores.add(new ErrorImportacion(f.fila, motivo));
            }
        }
        return errores;
    }

    private static String nulo(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private void persistir(FilaImport f, Despliegue despliegue, Contadores c) {
        Entidad entidad = obtenerEntidad(f.entidad);
        String[] segs = f.ubicacion == null ? new String[0]
                : Arrays.stream(f.ubicacion.split(">")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        Centro centro = obtenerCentro(segs, entidad, c);
        Ubicacion ubicacion = obtenerUbicacion(centro, segs, c);

        Equipo equipo = new Equipo();
        equipo.setHostname(f.hostname);
        equipo.setNumeroSerie(nulo(f.serie));
        equipo.setEtiquetaPatrimonial(nulo(f.etiqueta));
        equipo.setFabricante(f.fabricante);
        equipo.setModelo(f.modelo);
        equipo.setSistemaOperativo(f.sistemaOperativo);
        equipo.setProcesador(f.procesador);
        equipo.setCentro(centro);
        equipo.setUbicacion(ubicacion);
        equipo.setObservaciones(componerObservaciones(f));
        equipo.setEstado("PENDIENTE");
        equipo.setActivo(true);
        equipo = equipoRepository.save(equipo);

        if (f.ip != null && !f.ip.isBlank()) {
            RedConfig red = new RedConfig();
            red.setEquipo(equipo);
            red.setIp(f.ip);
            red.setTipoAsignacion(TipoAsignacionRed.DHCP);
            redConfigRepository.save(red);
        }

        DespliegueEquipo de = new DespliegueEquipo();
        de.setDespliegue(despliegue);
        de.setEquipo(equipo);
        de.setHostnameActual(f.hostname);
        de.setEstadoRenove(f.estadoRenove);
        de.setAnioRenove(f.anioRenove);
        de.setPerfilImagen(f.perfilImagen);
        de.setEstado("PENDIENTE");
        despliegueEquipoRepository.save(de);
        c.equipos++;
    }

    private Entidad obtenerEntidad(String path) {
        String[] segs = path == null ? new String[0]
                : Arrays.stream(path.split(">")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (segs.length == 0) {
            return null;
        }
        String raiz = segs.length > 1 ? segs[0] : null;
        String nombre = segs[segs.length - 1];
        return entidadRepository.findByNombre(nombre).orElseGet(() -> {
            Entidad e = new Entidad();
            e.setNombre(nombre);
            e.setEntidadRaiz(raiz);
            e.setActivo(true);
            return entidadRepository.save(e);
        });
    }

    private Centro obtenerCentro(String[] segs, Entidad entidad, Contadores c) {
        if (segs.length < 2) {
            return null;
        }
        String nombreCentro = segs[1];
        String codigo = generarCodigo(nombreCentro);
        if (codigo.isEmpty()) {
            return null;
        }
        return centroRepository.findByCodigo(codigo).orElseGet(() -> {
            Centro nuevo = new Centro();
            nuevo.setCodigo(codigo);
            nuevo.setNombre(nombreCentro);
            nuevo.setEntidad(entidad);
            nuevo.setActivo(true);
            c.centros++;
            return centroRepository.save(nuevo);
        });
    }

    private Ubicacion obtenerUbicacion(Centro centro, String[] segs, Contadores c) {
        if (centro == null || segs.length < 3) {
            return null;
        }
        String[] resto = Arrays.copyOfRange(segs, 2, segs.length);
        String nombreUbicacion = String.join(" > ", resto).trim();
        if (nombreUbicacion.isEmpty()) {
            return null;
        }
        return ubicacionRepository.findByCentroIdAndNombre(centro.getId(), nombreUbicacion)
                .orElseGet(() -> {
                    Ubicacion u = new Ubicacion();
                    u.setCentro(centro);
                    u.setNombre(nombreUbicacion);
                    u.setPlanta(plantaDe(resto));
                    u.setZona(null);
                    u.setActivo(true);
                    c.ubicaciones++;
                    return ubicacionRepository.save(u);
                });
    }

    private String componerObservaciones(FilaImport f) {
        List<String> partes = new ArrayList<>();
        if (f.estadoOrigen != null && !f.estadoOrigen.isBlank()) {
            partes.add("Origen: " + f.estadoOrigen);
        }
        if (f.idInventario != null) {
            partes.add("ID inventario: " + f.idInventario);
        }
        if (f.incidencias != null && !f.incidencias.isBlank()) {
            partes.add("Incidències: " + f.incidencias);
        }
        if (f.fechaCreacion != null && !f.fechaCreacion.isBlank()) {
            partes.add("Fecha creación: " + f.fechaCreacion);
        }
        if (f.ipsSecundarias != null && !f.ipsSecundarias.isEmpty()) {
            partes.add("IPs adicionales: " + String.join(", ", f.ipsSecundarias));
        }
        return String.join(" | ", partes);
    }

    private String plantaDe(String[] resto) {
        for (String s : resto) {
            if (PATRON_PLANTA.matcher(s).matches()) {
                return s;
            }
        }
        return null;
    }

    private DespliegueEquipoResponse toResponse(DespliegueEquipo de) {
        Equipo eq = de.getEquipo();
        String ip = redConfigRepository.findByEquipoId(eq.getId()).map(RedConfig::getIp).orElse(null);
        return new DespliegueEquipoResponse(de.getId(), de.getDespliegue().getId(), eq.getId(),
                de.getHostnameActual(), de.getHostnameNuevo(), de.getEstadoRenove(), de.getAnioRenove(),
                de.getPerfilImagen(), de.getEstado(),
                de.getTecnico() == null ? null : de.getTecnico().getId(),
                de.getTecnico() == null ? null : de.getTecnico().getNombreCompleto(),
                de.getFechaToma(),
                eq.getNumeroSerie(), eq.getFabricante(), eq.getModelo(), eq.getSistemaOperativo(),
                eq.getProcesador(),
                eq.getCentro() == null ? null : eq.getCentro().getNombre(),
                eq.getUbicacion() == null ? null : eq.getUbicacion().getNombre(),
                ip);
    }

    private String celda(Sheet hoja, int fila, Integer columna, DataFormatter df) {
        if (columna == null) {
            return "";
        }
        Row row = hoja.getRow(fila);
        if (row == null) {
            return "";
        }
        Cell celda = row.getCell(columna);
        if (celda == null) {
            return "";
        }
        String valor = df.formatCellValue(celda);
        return valor == null ? "" : valor.trim();
    }

    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static String generarCodigo(String nombre) {
        if (nombre == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String codigo = sinAcentos.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return codigo.length() > 20 ? codigo.substring(0, 20) : codigo;
    }

    private static final class Contadores {
        int equipos;
        int centros;
        int ubicaciones;
    }

    private static final class FilaImport {
        int fila;
        String hostname;
        String idInventario;
        String etiqueta;
        String entidad;
        String fabricante;
        String serie;
        String modelo;
        String sistemaOperativo;
        String ubicacion;
        String procesador;
        String incidencias;
        String estadoOrigen;
        String fechaCreacion;
        String ip;
        List<String> ipsSecundarias = new ArrayList<>();
        String estadoRenove;
        Integer anioRenove;
        String perfilImagen;
    }
}
