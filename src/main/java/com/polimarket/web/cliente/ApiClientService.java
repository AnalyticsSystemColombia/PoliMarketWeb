package com.polimarket.web.cliente;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ApiClientService {

    private final String API_BASE = "http://localhost:8081/api";
    private final RestTemplate rest = new RestTemplate();

    public <T> T get(String ruta, Class<T> tipoRespuesta) {
        return rest.getForObject(API_BASE + ruta, tipoRespuesta);
    }

    public Map post(String ruta, Object cuerpo, String usuarioHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (usuarioHeader != null) {
            headers.set("usuario", usuarioHeader);
        }
        HttpEntity<Object> peticion = new HttpEntity<>(cuerpo, headers);
        return rest.postForObject(API_BASE + ruta, peticion, Map.class);
    }
    public void deleteConUsuario(String ruta, String usuarioHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("usuario", usuarioHeader);
        rest.exchange(API_BASE + ruta, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }

}