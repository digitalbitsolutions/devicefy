package com.devicefy.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reconoce las columnas de un Excel de inventario mediante Ollama (coste 0, local).
 * Solo recibe la cabecera y unas pocas filas de ejemplo; el parseo masivo es determinista.
 */
@Slf4j
@Service
public class OllamaColumnMapper {

    private static final String CAMPO_NOM = "nom";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ollama.url}")
    private String url;

    @Value("${app.ollama.model}")
    private String model;

    @Value("${app.ollama.enabled}")
    private boolean enabled;

    @Value("${app.ollama.timeout-ms}")
    private int timeoutMs;

    public boolean estaActivo() {
        return enabled && url != null && !url.isBlank();
    }

    /**
     * Devuelve el mapeo campo interno -> índice de columna (0-based).
     * Vacío si Ollama no está disponible o la respuesta no es útil.
     */
    public Map<String, Integer> mapear(List<String> cabeceras, List<List<String>> ejemplos) {
        if (!estaActivo() || cabeceras.isEmpty()) {
            return Map.of();
        }
        String prompt = construirPrompt(cabeceras, ejemplos);
        try {
            String respuesta = llamar(prompt);
            Map<String, Object> json = extraerJson(respuesta);
            Map<String, Integer> mapa = new HashMap<>();
            for (Map.Entry<String, Object> e : json.entrySet()) {
                String campo = traducir(e.getKey());
                if (campo == null || e.getValue() == null) {
                    continue;
                }
                int idx;
                try {
                    idx = ((Number) e.getValue()).intValue();
                } catch (Exception ignore) {
                    continue;
                }
                if (idx < 0 || idx >= cabeceras.size()
                        || !cabeceraCoincide(campo, cabeceras.get(idx))) {
                    continue;
                }
                mapa.put(campo, idx);
            }
            log.info("Ollama mapeó {} columnas del Excel", mapa.size());
            return mapa;
        } catch (Exception e) {
            log.warn("Ollama no pudo mapear columnas: {}", e.getMessage());
            return Map.of();
        }
    }

    private String construirPrompt(List<String> cabeceras, List<List<String>> ejemplos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asistente que reconoce las columnas de una hoja de cálculo de inventario informático. ");
        sb.append("La primera línea son las cabeceras y las siguientes son filas de ejemplo.\n");
        sb.append("CABECERAS: ").append(JSON(cabeceras)).append("\n");
        sb.append("EJEMPLOS: ").append(JSON(ejemplos)).append("\n");
        sb.append("Asigna cada columna (por su índice, empezando en 0) a UNO de estos campos: ");
        sb.append("hostname, etiqueta, entidad, fabricante, serie, modelo, ubicacion, sistema_operativo, ");
        sb.append("procesador, estado, renove, ip, fecha_creacion, perfil_maqueta, incidencias. ");
        sb.append("REGLAS ESTRICTAS: la columna del nombre del equipo ('Nom'/'Nombre'/'Equipo') SIEMPRE debe ");
        sb.append("mapearse a 'hostname' e incluirse en la respuesta. ");
        sb.append("Pistas: 'Nº de sèrie'/'Serie' es serie; 'Xarxa'/'Red' suele ser ip; ");
        sb.append("'Sistema Operatiu' es sistema_operativo. ");
        sb.append("Usa únicamente índices que existan. Si una columna no corresponde a ningún campo, omítela. ");
        sb.append("RESPONDE ÚNICAMENTE con JSON válido, sin comentarios ni texto adicional, formato: ");
        sb.append("{\"hostname\":0,\"fabricante\":1,\"serie\":2}");
        return sb.toString();
    }

    private String JSON(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String llamar(String prompt) throws Exception {
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "format", "json",
                "options", Map.of("temperature", 0));
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/generate"))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        Map<String, Object> resultado = objectMapper.readValue(response.body(), Map.class);
        Object texto = resultado.get("response");
        return texto == null ? "" : texto.toString();
    }

    private Map<String, Object> extraerJson(String respuesta) throws Exception {
        String limpiada = respuesta.trim();
        if (limpiada.startsWith("```")) {
            limpiada = limpiada.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        int inicio = limpiada.indexOf('{');
        int fin = limpiada.lastIndexOf('}');
        if (inicio >= 0 && fin > inicio) {
            limpiada = limpiada.substring(inicio, fin + 1);
        }
        return objectMapper.readValue(limpiada, Map.class);
    }

    private String traducir(String campo) {
        if (campo == null) {
            return null;
        }
        return switch (campo.trim().toLowerCase().replace('-', '_')) {
            case "hostname", "nombre", "equipo" -> CAMPO_NOM;
            case "etiqueta", "etiquetapatrimonial" -> "etiqueta";
            case "entidad", "entitat" -> "entitat";
            case "fabricante", "fabricant", "marca" -> "fabricante";
            case "serie", "numerodeserie", "numero_de_serie" -> "serie";
            case "modelo", "model" -> "modelo";
            case "ubicacion", "ubicacio", "localizacion" -> "ubicacion";
            case "sistema_operativo", "sistemaoperativo", "so" -> "so_nom";
            case "procesador", "cpu" -> "procesador";
            case "estado", "estat" -> "estado";
            case "renove", "rhe", "estadorenove" -> "rhe";
            case "ip", "xarxa", "red" -> "ip";
            case "fecha_creacion", "fechadecreacio", "fecha" -> "fecha";
            case "perfil_maqueta", "maqueta", "perfilimagen" -> "maqueta";
            case "incidencias", "suport", "observaciones" -> "incidencias";
            default -> null;
        };
    }

    /**
     * Valida que la cabecera situada en el índice propuesto por la IA sea coherente
     * con el campo interno que se le va a asignar. Evita índices erróneos u
     * fuera de rango de modelos pequeños.
     */
    private boolean cabeceraCoincide(String campo, String cabecera) {
        String k = normalizar(cabecera);
        if (k.isEmpty()) {
            return false;
        }
        return switch (campo) {
            case "nom" -> k.contains("nom") || k.contains("nombre") || k.contains("equip")
                    || k.contains("hostname");
            case "etiqueta" -> k.contains("etiqueta");
            case "entitat" -> k.contains("entitat") || k.contains("entidad");
            case "fabricante" -> k.contains("fabric") || k.contains("marca");
            case "serie" -> k.contains("serie") || k.contains("serial");
            case "modelo" -> k.contains("model");
            case "ubicacion" -> k.contains("ubicac") || k.contains("localizac");
            case "so_nom" -> k.contains("sistema") || k.contains("operatiu") || k.contains("operativo")
                    || k.startsWith("so");
            case "procesador" -> k.contains("procesador") || k.contains("cpu");
            case "estado" -> k.contains("estado") || k.contains("estat");
            case "rhe" -> k.contains("rhe") || k.contains("renove");
            case "ip" -> k.contains("ip") || k.contains("xarxa") || k.contains("red");
            case "fecha" -> k.contains("fecha") || k.contains("data");
            case "maqueta" -> k.contains("maqueta") || k.contains("perfil");
            case "incidencias" -> k.contains("incidenc") || k.contains("suport") || k.contains("observac");
            default -> false;
        };
    }

    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
