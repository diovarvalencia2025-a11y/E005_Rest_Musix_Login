package com.ipartek.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ipartek.pojos.MsgEstado;
import com.ipartek.pojos.PreguntasUsuario;

@Service
public class PreguntasUsuarioServicioImp implements PreguntasUsuarioServicio {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${api.login.url:http://localhost:9090}")
	private String apiUrl;

	private String getBaseUrl() {
		String base = apiUrl;
		if (base.endsWith("/api")) {
			base = base.substring(0, base.length() - 4);
		}
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/api/v1/preguntasusuarios";
	}

	@Override
	public void insertar(PreguntasUsuario pu) {
		try {
			restTemplate.postForObject(getBaseUrl() + "/", pu, Object.class);
		} catch (Exception e) {
			System.err.println("Error al insertar respuesta a través de /api/v1/preguntasusuarios/: " + e.getMessage());
			try {
				// Fallback secundario
				restTemplate.postForObject("http://localhost:9090/api/preguntas-usuario", pu, Object.class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public boolean validarRespuestas(String username, String r1, String r2, String r3) {
		try {
			String url = String.format("%s/validar?username=%s&r1=%s&r2=%s&r3=%s", getBaseUrl(), username, r1, r2, r3);
			ResponseEntity<MsgEstado> resp = restTemplate.postForEntity(url, null, MsgEstado.class);
			return resp.getStatusCode().is2xxSuccessful();
		} catch (Exception e) {
			try {
				String fallbackUrl = String.format("http://localhost:9090/api/preguntas-usuario/validar?username=%s&r1=%s&r2=%s&r3=%s",
						username, r1, r2, r3);
				ResponseEntity<MsgEstado> resp = restTemplate.postForEntity(fallbackUrl, null, MsgEstado.class);
				return resp.getStatusCode().is2xxSuccessful();
			} catch (Exception ex) {
				return false;
			}
		}
	}
}
