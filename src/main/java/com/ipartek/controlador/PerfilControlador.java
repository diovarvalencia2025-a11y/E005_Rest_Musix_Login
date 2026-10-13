package com.ipartek.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ipartek.pojos.Pregunta;
import com.ipartek.pojos.PreguntasUsuario;
import com.ipartek.pojos.Usuario;
import com.ipartek.servicios.PreguntaServicio;
import com.ipartek.servicios.PreguntasUsuarioServicio;
import com.ipartek.servicios.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

@Controller
public class PerfilControlador {

	@Autowired
	private UsuarioServicio usuarioServicio;

	@Autowired
	private PreguntaServicio preguntaServicio;

	@Autowired
	private PreguntasUsuarioServicio pregUsuServicio;

	@GetMapping("/perfil")
	public String verPerfil(HttpSession session, Model model) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null) {
			return "redirect:/login";
		}

		List<Pregunta> preguntas = preguntaServicio.obtenerTodas();
		model.addAttribute("usuario", usu);
		model.addAttribute("preguntas", preguntas);

		return "plantillas/perfil";
	}

	@PostMapping("/perfil")
	public String guardarPerfil(@RequestParam String user,
			@RequestParam(required = false) String pass,
			@RequestParam(value = "foto", required = false) org.springframework.web.multipart.MultipartFile archivoFoto,
			@RequestParam(required = false) Integer pregunta1,
			@RequestParam(required = false) String respuesta1,
			@RequestParam(required = false) Integer pregunta2,
			@RequestParam(required = false) String respuesta2,
			@RequestParam(required = false) Integer pregunta3,
			@RequestParam(required = false) String respuesta3,
			HttpSession session,
			RedirectAttributes redirect,
			Model model) {

		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null) {
			return "redirect:/login";
		}

		usu.setUser(user);
		if (pass != null && !pass.isBlank()) {
			usuarioServicio.cambiarPassword(usu.getId(), pass);
		}

		if (archivoFoto != null && !archivoFoto.isEmpty()) {
			String nombreFoto = usuarioServicio.subirFoto(archivoFoto);
			usu.setFoto(nombreFoto);
		}

		Usuario mod = usuarioServicio.modificar(usu.getId(), usu);
		if (mod != null) {
			session.setAttribute("usuario", mod);
		}

		if (pregunta1 != null && respuesta1 != null && !respuesta1.isBlank()) {
			pregUsuServicio.insertar(new PreguntasUsuario(null, usu, new Pregunta(pregunta1, null), respuesta1));
		}
		if (pregunta2 != null && respuesta2 != null && !respuesta2.isBlank()) {
			pregUsuServicio.insertar(new PreguntasUsuario(null, usu, new Pregunta(pregunta2, null), respuesta2));
		}
		if (pregunta3 != null && respuesta3 != null && !respuesta3.isBlank()) {
			pregUsuServicio.insertar(new PreguntasUsuario(null, usu, new Pregunta(pregunta3, null), respuesta3));
		}

		redirect.addFlashAttribute("f_mensaje", "Perfil actualizado con éxito");
		return "redirect:/perfil";
	}
}