package com.ipartek.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiCliente {

    @Autowired
    private RestTemplate restTemplate;

    public <T> T get(String url, Class<T> tipo) {
        return restTemplate.getForObject(url, tipo);
    }

    public <T> T post(String url, Object cuerpo, Class<T> tipo) {
        return restTemplate.postForObject(url, cuerpo, tipo);
    }

    // PUT que no devuelve nada (para bloquear, banear, reportar...)
    public void put(String url, Object cuerpo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(cuerpo, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    // PUT que devuelve la respuesta (para modificarPerfil)
    public <T> T put(String url, Object cuerpo, Class<T> tipo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(cuerpo, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, tipo).getBody();
    }

    public void delete(String url) {
        restTemplate.delete(url);
    }
}