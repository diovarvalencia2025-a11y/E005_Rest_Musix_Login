package com.ipartek.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ipartek.componentes.JwtUtil;
import com.ipartek.pojos.Pregunta;
import com.ipartek.pojos.PreguntasUsuario;
import com.ipartek.pojos.Usuario;
import com.ipartek.servicios.PreguntaServicio;
import com.ipartek.servicios.PreguntasUsuarioServicio;
import com.ipartek.servicios.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginControlador {

	@Autowired
	private UsuarioServicio usuarioServicio;

	@Autowired
	private PreguntaServicio preguntaServicio;

	@Autowired
	private PreguntasUsuarioServicio pregUsuServicio;

	@Autowired(required = false)
	private JwtUtil jwtUtil;

	// ---------- VISTA DE LOGIN ----------
	@GetMapping("/login")
	public String mostrarLogin(Model model, HttpSession session) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu != null) {
			if (usu.getRol_fk() != null && "ADMIN".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
				return "redirect:/admin/canciones";
			}
			return "redirect:/canciones";
		}
		model.addAttribute("obj_usuario", new Usuario());
		return "plantillas/login";
	}

	// ---------- VALIDAR LOGIN (Ambos endpoints: /login y /ValidarLogin del profesor) ----------
	@PostMapping({"/login", "/ValidarLogin"})
	public String validarLogin(@RequestParam(value = "user", required = false) String paramUser,
			@RequestParam(value = "pass", required = false) String paramPass,
			@ModelAttribute("obj_usuario") Usuario objUsuario,
			HttpSession session,
			Model model,
			RedirectAttributes redirect) {

		String usuarioAValidar = (paramUser != null && !paramUser.isBlank()) ? paramUser : (objUsuario != null ? objUsuario.getUser() : null);
		String passAValidar = (paramPass != null && !paramPass.isBlank()) ? paramPass : (objUsuario != null ? objUsuario.getPass() : null);

		if (usuarioAValidar == null || usuarioAValidar.isBlank() || passAValidar == null || passAValidar.isBlank()) {
			model.addAttribute("error", "Por favor introduce usuario y contraseña");
			model.addAttribute("obj_usuario", new Usuario());
			return "plantillas/login";
		}

		try {
			Usuario usu = usuarioServicio.validarLogin(usuarioAValidar, passAValidar);
			if (usu != null) {
				session.setAttribute("usuario", usu);
				session.setAttribute("usuario_logeado", usu);
				session.setAttribute("s_usuario_nombre", usu.getUser());
				String rol = (usu.getRol_fk() != null && usu.getRol_fk().getNombre() != null) ? usu.getRol_fk().getNombre() : "USER";
				session.setAttribute("s_rol", rol);

				if (jwtUtil != null) {
					String token = jwtUtil.generateToken(usu.getUser(), rol.toUpperCase());
					session.setAttribute("s_token", token);
				}

				if ("ADMIN".equalsIgnoreCase(rol)) {
					return "redirect:/admin/canciones";
				}
				return "redirect:/canciones";
			}
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("obj_usuario", new Usuario());
			return "plantillas/login";
		}

		model.addAttribute("error", "Usuario o contraseña incorrectos");
		model.addAttribute("obj_usuario", new Usuario());
		return "plantillas/login";
	}

	// ---------- CREAR USUARIO / REGISTRO ----------
	@GetMapping({"/CrearUsuario", "/registro"})
	public String crearUsuario(Model model, HttpSession session) {
		List<Pregunta> preguntas = preguntaServicio.obtenerTodas();
		model.addAttribute("obj_usuario", new Usuario());
		model.addAttribute("preguntas", preguntas);
		return "plantillas/registro";
	}

	@PostMapping("/CancelarCrearUsuario")
	public String cancelarCrearUsuario(RedirectAttributes redirect) {
		redirect.addFlashAttribute("f_mensaje", "Usuario Cancelado");
		return "redirect:/";
	}

	@PostMapping({"/CrearUsuarioReal", "/registro"})
	public String crearUsuarioReal(@ModelAttribute Usuario usuario,
			@RequestParam(value = "archivoFoto", required = false) org.springframework.web.multipart.MultipartFile archivoFoto,
			@RequestParam(value = "fotoFile", required = false) org.springframework.web.multipart.MultipartFile fotoFile,
			@RequestParam(value = "pregunta1", required = false) Integer preg1,
			@RequestParam(value = "respuesta1", required = false) String resp1,
			@RequestParam(value = "pregunta2", required = false) Integer preg2,
			@RequestParam(value = "respuesta2", required = false) String resp2,
			@RequestParam(value = "pregunta3", required = false) Integer preg3,
			@RequestParam(value = "respuesta3", required = false) String resp3,
			@RequestParam(value = "preg1", required = false) Integer p1Alt,
			@RequestParam(value = "resp1", required = false) String r1Alt,
			@RequestParam(value = "preg2", required = false) Integer p2Alt,
			@RequestParam(value = "resp2", required = false) String r2Alt,
			@RequestParam(value = "preg3", required = false) Integer p3Alt,
			@RequestParam(value = "resp3", required = false) String r3Alt,
			RedirectAttributes redirect,
			Model model) {

		Integer idPreg1 = preg1 != null ? preg1 : p1Alt;
		String textoResp1 = resp1 != null ? resp1 : r1Alt;
		Integer idPreg2 = preg2 != null ? preg2 : p2Alt;
		String textoResp2 = resp2 != null ? resp2 : r2Alt;
		Integer idPreg3 = preg3 != null ? preg3 : p3Alt;
		String textoResp3 = resp3 != null ? resp3 : r3Alt;

		try {
			org.springframework.web.multipart.MultipartFile fileToUpload =
					(archivoFoto != null && !archivoFoto.isEmpty()) ? archivoFoto :
					(fotoFile != null && !fotoFile.isEmpty()) ? fotoFile : null;

			if (fileToUpload != null && !fileToUpload.isEmpty()) {
				String nombreGuardado = usuarioServicio.subirFoto(fileToUpload);
				usuario.setFoto(nombreGuardado);
			} else if (usuario.getFoto() == null || usuario.getFoto().isBlank()) {
				usuario.setFoto("default.png");
			}

			Usuario creado = usuarioServicio.insertar(usuario);
			if (creado != null) {
				// Guardar preguntas de seguridad
				if (idPreg1 != null && textoResp1 != null && !textoResp1.isBlank()) {
					pregUsuServicio.insertar(new PreguntasUsuario(null, creado, new Pregunta(idPreg1, null), textoResp1.trim()));
				}
				if (idPreg2 != null && textoResp2 != null && !textoResp2.isBlank()) {
					pregUsuServicio.insertar(new PreguntasUsuario(null, creado, new Pregunta(idPreg2, null), textoResp2.trim()));
				}
				if (idPreg3 != null && textoResp3 != null && !textoResp3.isBlank()) {
					pregUsuServicio.insertar(new PreguntasUsuario(null, creado, new Pregunta(idPreg3, null), textoResp3.trim()));
				}

				redirect.addFlashAttribute("f_mensaje", "Usuario creado con éxito. Inicia sesión.");
				return "redirect:/login";
			}
		} catch (Exception e) {
			model.addAttribute("error", "Error creando usuario: " + e.getMessage());
			model.addAttribute("preguntas", personaPreguntasOpcionales());
			return "plantillas/registro";
		}

		model.addAttribute("error", "No se pudo crear el usuario");
		return "plantillas/registro";
	}

	private List<Pregunta> personaPreguntasOpcionales() {
		try {
			return preguntaServicio.obtenerTodas();
		} catch (Exception e) {
			return List.of();
		}
	}

	// ---------- RECUPERAR CONTRASEÑA ----------
	@GetMapping("/RecuperarPass")
	public String recuperarPass(Model model) {
		return "plantillas/recuperar_pass";
	}

	@PostMapping("/CancelarRecuperarPass")
	public String cancelarRecuperarPass(RedirectAttributes redirect) {
		redirect.addFlashAttribute("f_mensaje", "recuperar pass cancelado");
		return "redirect:/";
	}

	@PostMapping("/RestaurarPassFrm")
	public String restaurarPassFrm(@RequestParam("user") String username,
			@RequestParam(value = "respuesta1", required = false) String r1,
			@RequestParam(value = "respuesta2", required = false) String r2,
			@RequestParam(value = "respuesta3", required = false) String r3,
			HttpSession session,
			RedirectAttributes redirect,
			Model model) {

		if (username == null || username.isBlank()) {
			model.addAttribute("f_mensaje", "El nombre de usuario es obligatorio");
			return "plantillas/recuperar_pass";
		}

		boolean validas = pregUsuServicio.validarRespuestas(username, r1, r2, r3);
		if (validas) {
			session.setAttribute("user_recuperando", username);
			model.addAttribute("user", username);
			return "plantillas/restaurar_pass";
		}

		model.addAttribute("f_mensaje", "Las respuestas de seguridad no coinciden con las registradas");
		return "plantillas/recuperar_pass";
	}

	@PostMapping("/CancelarRestaurarPass")
	public String cancelarRestaurarPass(HttpSession session, RedirectAttributes redirect) {
		session.removeAttribute("user_recuperando");
		redirect.addFlashAttribute("f_mensaje", "Restaurar pass cancelado");
		return "redirect:/";
	}

	@PostMapping("/RestaurarPass")
	public String restaurarPassReal(@RequestParam(value = "pass", required = false) String nuevoPass,
			HttpSession session,
			RedirectAttributes redirect,
			Model model) {

		String username = (String) session.getAttribute("user_recuperando");
		if (username == null) {
			redirect.addFlashAttribute("f_mensaje", "Sesión de recuperación expirada");
			return "redirect:/";
		}

		if (nuevoPass == null || nuevoPass.isBlank()) {
			model.addAttribute("f_mensaje", "La nueva contraseña es obligatoria");
			return "plantillas/restaurar_pass";
		}

		Usuario u = usuarioServicio.obtenerPorUser(username);
		if (u != null) {
			usuarioServicio.cambiarPassword(u.getId(), nuevoPass);
			session.removeAttribute("user_recuperando");
			redirect.addFlashAttribute("f_mensaje", "Password restaurado");
			return "redirect:/";
		}

		redirect.addFlashAttribute("f_mensaje", "Error restaurando contraseña");
		return "redirect:/";
	}

	// ---------- CERRAR SESIÓN ----------
	@GetMapping({"/logout", "/CerrarSession"})
	public String cerrarSesion(HttpSession session, RedirectAttributes redirect) {
		session.invalidate();
		redirect.addFlashAttribute("f_mensaje", "Sesión cerrada correctamente");
		return "redirect:/login";
	}
}