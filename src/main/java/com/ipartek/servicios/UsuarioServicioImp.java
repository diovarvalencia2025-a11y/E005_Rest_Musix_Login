package com.ipartek.servicios;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ipartek.pojos.MsgEstado;
import com.ipartek.pojos.Rol;
import com.ipartek.pojos.Usuario;

@Service
public class UsuarioServicioImp implements UsuarioServicio {

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
		return base + "/api/v1/usuarios";
	}

	@Override
	public List<Usuario> obtenerTodos() {
		try {
			Usuario[] res = restTemplate.getForObject(getBaseUrl() + "/", Usuario[].class);
			return res != null ? Arrays.asList(res) : List.of();
		} catch (Exception e) {
			return List.of();
		}
	}

	@Override
	public Usuario obtenerPorId(Integer id) {
		try {
			return restTemplate.getForObject(getBaseUrl() + "/" + id, Usuario.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Usuario obtenerPorUser(String user) {
		try {
			return restTemplate.getForObject(getBaseUrl() + "/user/" + user, Usuario.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Usuario insertar(Usuario usuario) {
		try {
			return restTemplate.postForObject(getBaseUrl() + "/", usuario, Usuario.class);
		} catch (Exception e) {
			System.err.println("Error insertando usuario en API: " + e.getMessage());
			return null;
		}
	}

	@Override
	public Usuario modificar(Integer id, Usuario usuario) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Usuario> entity = new HttpEntity<>(usuario, headers);
			ResponseEntity<Usuario> response = restTemplate.exchange(getBaseUrl() + "/" + id, HttpMethod.PUT, entity,
					Usuario.class);
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
	public Usuario validarLogin(String user, String pass) {
		try {
			Usuario loginReq = new Usuario();
			loginReq.setUser(user);
			loginReq.setPass(pass);

			ResponseEntity<Usuario> resp = restTemplate.postForEntity(getBaseUrl() + "/login", loginReq,
					Usuario.class);
			if (resp.getStatusCode() == HttpStatus.OK) {
				return resp.getBody();
			}
		} catch (HttpClientErrorException.Forbidden e) {
			throw new RuntimeException("Cuenta bloqueada o baneada por superar intentos o acción de administrador.");
		} catch (HttpClientErrorException.Unauthorized e) {
			throw new RuntimeException("Usuario o contraseña incorrectos.");
		} catch (HttpClientErrorException.BadRequest e) {
			throw new RuntimeException("Credenciales incorrectas.");
		} catch (Exception e) {
			throw new RuntimeException("Error conectando con el servidor de autenticación: " + e.getMessage());
		}
		return null;
	}

	@Override
	public void resetearIntentos(Integer id) {
		try {
			restTemplate.put(getBaseUrl() + "/" + id + "/resetear-intentos", null);
		} catch (Exception ignored) {
		}
	}

	@Override
	public void cambiarPassword(Integer id, String nuevoPass) {
		try {
			restTemplate.put(getBaseUrl() + "/" + id + "/cambiar-pass?nuevoPass=" + nuevoPass, null);
		} catch (Exception ignored) {
		}
	}

	@Override
	public void banear(Integer id) {
		try {
			restTemplate.put(getBaseUrl() + "/" + id + "/banear", null);
		} catch (Exception e) {
			try {
				Usuario u = obtenerPorId(id);
				if (u != null) {
					u.setRol_fk(new Rol(4, "BANEADO"));
					u.setIntentos(3);
					modificar(id, u);
				}
			} catch (Exception ex) {
				System.err.println("Error al banear usuario " + id + ": " + ex.getMessage());
			}
		}
	}

	@Override
	public void desbanear(Integer id) {
		try {
			restTemplate.put(getBaseUrl() + "/" + id + "/desbanear", null);
		} catch (Exception e) {
			try {
				Usuario u = obtenerPorId(id);
				if (u != null) {
					u.setRol_fk(new Rol(2, "USUARIO"));
					u.setIntentos(0);
					modificar(id, u);
				}
			} catch (Exception ex) {
				System.err.println("Error al desbanear usuario " + id + ": " + ex.getMessage());
			}
		}
	}

	@Override
	public String subirFoto(MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			return "default.png";
		}

		try {
			// 1. Subir vía REST a la API
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("archivo", new ByteArrayResource(archivo.getBytes()) {
				@Override
				public String getFilename() {
					return archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "avatar.png";
				}
			});

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			ResponseEntity<MsgEstado> response = restTemplate.postForEntity(getBaseUrl() + "/upload-foto", requestEntity,
					MsgEstado.class);

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return response.getBody().getMensaje();
			}
		} catch (Exception e) {
			System.err.println("Error subiendo foto al servidor API: " + e.getMessage());
		}

		return "default.png";
	}
}
