package com.ipartek.servicios;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ipartek.pojos.Cancion;

@Service
public class CancionServicioImp implements CancionServicio {

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
		return base + "/api/v1/canciones";
	}

	@Override
	public List<Cancion> obtenerTodas() {
		try {
			Cancion[] res = restTemplate.getForObject(getBaseUrl() + "/", Cancion[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public List<Cancion> obtenerTodasAdmin() {
		try {
			Cancion[] res = restTemplate.getForObject(getBaseUrl() + "/admin", Cancion[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public Cancion obtenerPorId(Integer id) {
		try {
			return restTemplate.getForObject(getBaseUrl() + "/" + id, Cancion.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<Cancion> obtenerPorUsuario(Integer usuarioId) {
		try {
			Cancion[] res = restTemplate.getForObject(getBaseUrl() + "/usuario/" + usuarioId, Cancion[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public List<Cancion> buscarFiltro(Integer idUsu, Integer idEst, Integer duracionMin, Integer duracionMax,
			Boolean reportada) {
		try {
			StringBuilder url = new StringBuilder(getBaseUrl() + "/filtro?");
			if (idUsu != null) url.append("idUsu=").append(idUsu).append("&");
			if (idEst != null) url.append("idEst=").append(idEst).append("&");
			if (duracionMin != null) url.append("duracionMin=").append(duracionMin).append("&");
			if (duracionMax != null) url.append("duracionMax=").append(duracionMax).append("&");
			if (reportada != null) url.append("reportada=").append(reportada).append("&");

			Cancion[] res = restTemplate.getForObject(url.toString(), Cancion[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public Cancion insertar(Cancion cancion) {
		try {
			return restTemplate.postForObject(getBaseUrl() + "/", cancion, Cancion.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Cancion modificar(Cancion cancion) {
		if (cancion == null || cancion.getId() == null) {
			return null;
		}
		return modificar(cancion.getId(), cancion);
	}

	@Override
	public Cancion modificar(Integer id, Cancion cancion) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Cancion> entity = new HttpEntity<>(cancion, headers);
			ResponseEntity<Cancion> response = restTemplate.exchange(getBaseUrl() + "/" + id, HttpMethod.PUT, entity,
					Cancion.class);
			return response.getBody();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Boolean borrar(Integer id) {
		try {
			restTemplate.delete(getBaseUrl() + "/" + id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Boolean reportar(Integer id) {
		try {
			restTemplate.put(getBaseUrl() + "/" + id + "/reportar", null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}