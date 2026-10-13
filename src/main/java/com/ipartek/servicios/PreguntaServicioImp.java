package com.ipartek.servicios;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ipartek.pojos.Pregunta;

@Service
public class PreguntaServicioImp implements PreguntaServicio {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${api.musix.url:http://localhost:9090}")
	private String apiUrl;

	private String getBaseUrl() {
		String base = apiUrl;
		if (base.endsWith("/api")) {
			base = base.substring(0, base.length() - 4);
		}
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/api/v1/preguntas";
	}

	@Override
	public List<Pregunta> obtenerTodas() {
		try {
			Pregunta[] res = restTemplate.getForObject(getBaseUrl() + "/", Pregunta[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public Pregunta obtenerPorId(Integer id) {
		try {
			return restTemplate.getForObject(getBaseUrl() + "/" + id, Pregunta.class);
		} catch (Exception e) {
			return null;
		}
	}
}