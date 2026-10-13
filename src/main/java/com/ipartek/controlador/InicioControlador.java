package com.ipartek.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ipartek.pojos.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
public class InicioControlador {

	@GetMapping("/")
	public String inicio(Model model, HttpSession session) {
		if (session.getAttribute("usuario") != null) {
			Usuario u = (Usuario) session.getAttribute("usuario");
			if (u.getRol_fk() != null && "ADMIN".equalsIgnoreCase(u.getRol_fk().getNombre())) {
				return "redirect:/admin/canciones";
			}
			return "redirect:/canciones";
		}
		model.addAttribute("obj_usuario", new Usuario());
		return "plantillas/login";
	}
}
