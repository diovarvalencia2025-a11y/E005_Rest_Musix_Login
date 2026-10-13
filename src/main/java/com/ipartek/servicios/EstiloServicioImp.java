package com.ipartek.servicios;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ipartek.pojos.Estilo;

@Service
public class EstiloServicioImp implements EstiloServicio {

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
		return base + "/api/v1/estilos";
	}

	@Override
	public List<Estilo> obtenerTodos() {
		try {
			Estilo[] res = restTemplate.getForObject(getBaseUrl() + "/", Estilo[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public Estilo obtenerPorId(Integer id) {
		try {
			return restTemplate.getForObject(getBaseUrl() + "/" + id, Estilo.class);
		} catch (Exception e) {
			return null;
		}
	}
}