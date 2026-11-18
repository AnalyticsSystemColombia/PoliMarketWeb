package com.polimarket.web.cliente;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiClientService {

    private final String API_BASE = "http://localhost:8081/api";
    private final RestTemplate rest = new RestTemplate();

    public <T> T get(String ruta, Class<T> tipoRespuesta) {
        try {
            return rest.getForObject(API_BASE + ruta, tipoRespuesta);
        } catch (Exception e) {
            if (tipoRespuesta == List.class) {
                return (T) Collections.emptyList();
            }
            return null;
        }
    }

    public Map post(String ruta, Object cuerpo, String usuarioHeader) {
        return enviarConCuerpo(HttpMethod.POST, ruta, cuerpo, usuarioHeader);
    }

    public Map put(String ruta, Object cuerpo, String usuarioHeader) {
        return enviarConCuerpo(HttpMethod.PUT, ruta, cuerpo, usuarioHeader);
    }

    public void delete(String ruta) {
        rest.delete(API_BASE + ruta);
    }

    public void deleteConUsuario(String ruta, String usuarioHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("usuario", usuarioHeader);
        rest.exchange(API_BASE + ruta, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }

    private Map enviarConCuerpo(HttpMethod metodo, String ruta, Object cuerpo, String usuarioHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (usuarioHeader != null) {
            headers.set("usuario", usuarioHeader);
        }
        HttpEntity<Object> peticion = new HttpEntity<>(cuerpo, headers);
        return rest.exchange(API_BASE + ruta, metodo, peticion, Map.class).getBody();
    }
}
