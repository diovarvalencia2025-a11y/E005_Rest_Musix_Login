package com.ipartek.controlador;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ipartek.pojos.Rol;
import com.ipartek.pojos.Usuario;
import com.ipartek.servicios.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminUsuariosControlador {

	@Autowired
	private UsuarioServicio usuarioServicio;

	@GetMapping("/admin/usuarios")
	public String listar(@RequestParam(required = false) String texto,
			@RequestParam(required = false) String rol,
			@RequestParam(required = false) String estado,
			@RequestParam(defaultValue = "1") Integer page,
			HttpSession session,
			Model model) {

		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		List<Usuario> todos = usuarioServicio.obtenerTodos();
		if (todos == null) {
			todos = List.of();
		}

		// Conteo de usuarios para los paneles informativos
		long totalUsuarios = todos.size();
		long totalBaneados = todos.stream()
				.filter(Usuario::isBaneado)
				.count();
		long totalActivos = todos.stream()
				.filter(u -> !u.isBaneado())
				.count();
		long totalAdmins = todos.stream()
				.filter(u -> u.getRol_fk() != null && "ADMIN".equalsIgnoreCase(u.getRol_fk().getNombre()))
				.count();

		List<Usuario> filtrados = todos.stream()
				.filter(u -> {
					boolean cumpleTexto = (texto == null || texto.isBlank()) ||
							(u.getUser() != null && u.getUser().toLowerCase().contains(texto.toLowerCase().trim()));

					boolean cumpleRol = (rol == null || rol.isBlank() || "TODOS".equalsIgnoreCase(rol)) ||
							(u.getRol_fk() != null && u.getRol_fk().getNombre() != null && u.getRol_fk().getNombre().equalsIgnoreCase(rol));

					boolean cumpleEstado = true;
					if (estado != null && !estado.isBlank() && !"TODOS".equalsIgnoreCase(estado)) {
						if ("ACTIVO".equalsIgnoreCase(estado)) {
							cumpleEstado = !u.isBaneado();
						} else if ("BLOQUEADO".equalsIgnoreCase(estado) || "BANEADO".equalsIgnoreCase(estado)) {
							cumpleEstado = u.isBaneado();
						}
					}

					return cumpleTexto && cumpleRol && cumpleEstado;
				})
				.collect(Collectors.toList());

		int pageSize = 6;
		int totalItems = filtrados.size();
		int totalPaginas = (int) Math.ceil((double) totalItems / pageSize);
		if (totalPaginas < 1) totalPaginas = 1;
		if (page < 1) page = 1;
		if (page > totalPaginas) page = totalPaginas;

		int fromIndex = (page - 1) * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, totalItems);

		List<Usuario> paginaUsuarios = (fromIndex <= toIndex && fromIndex < totalItems)
				? filtrados.subList(fromIndex, toIndex)
				: List.of();

		int mostrandoDesde = (totalItems > 0) ? fromIndex + 1 : 0;
		int mostrandoHasta = toIndex;

		model.addAttribute("usuarios", paginaUsuarios);
		model.addAttribute("usuario", usu);

		model.addAttribute("totalUsuarios", totalUsuarios);
		model.addAttribute("totalActivos", totalActivos);
		model.addAttribute("totalBaneados", totalBaneados);
		model.addAttribute("totalAdmins", totalAdmins);

		model.addAttribute("paginaActual", page);
		model.addAttribute("totalPaginas", totalPaginas);
		model.addAttribute("totalItems", totalItems);
		model.addAttribute("mostrandoDesde", mostrandoDesde);
		model.addAttribute("mostrandoHasta", mostrandoHasta);

		model.addAttribute("fTexto", texto);
		model.addAttribute("fRol", rol);
		model.addAttribute("fEstado", estado);

		return "plantillas/admin-usuarios";
	}

	@PostMapping("/admin/usuarios/crear")
	public String crearUsuario(@RequestParam String user,
			@RequestParam String pass,
			@RequestParam(defaultValue = "USER") String rol,
			HttpSession session,
			RedirectAttributes redirect) {

		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = md.digest((pass + "Galea").getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hashBytes) {
				sb.append(String.format("%02x", b));
			}

			Usuario nuevo = new Usuario();
			nuevo.setUser(user.trim());
			nuevo.setPass(sb.toString());
			nuevo.setFoto("avatar.png");
			nuevo.setIntentos(0);
			nuevo.setRol_fk(new Rol(null, rol.toUpperCase()));

			Usuario creado = usuarioServicio.insertar(nuevo);
			if (creado != null) {
				redirect.addFlashAttribute("f_mensaje", "Usuario " + user + " creado con éxito.");
			} else {
				redirect.addFlashAttribute("f_error", "No se pudo crear el usuario. Nombre duplicado o inválido.");
			}
		} catch (Exception e) {
			redirect.addFlashAttribute("f_error", "Error al crear el usuario: " + e.getMessage());
		}

		return "redirect:/admin/usuarios";
	}

	@org.springframework.web.bind.annotation.RequestMapping(value = {"/admin/usuarios/desbloquear/{id}", "/admin/usuarios/desbanear/{id}"}, method = {org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
	public String desbloquear(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		usuarioServicio.desbanear(id);
		redirect.addFlashAttribute("f_mensaje", "Usuario desbaneado y reactivado con éxito.");
		return "redirect:/admin/usuarios";
	}

	@org.springframework.web.bind.annotation.RequestMapping(value = {"/admin/usuarios/bloquear/{id}", "/admin/usuarios/banear/{id}"}, method = {org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
	public String bloquear(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		usuarioServicio.banear(id);
		redirect.addFlashAttribute("f_mensaje", "Usuario baneado del sistema con éxito.");
		return "redirect:/admin/usuarios";
	}

	@PostMapping("/admin/usuarios/cambiar-rol/{id}")
	public String cambiarRol(@PathVariable Integer id,
			@RequestParam String rol,
			HttpSession session,
			RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		Usuario u = usuarioServicio.obtenerPorId(id);
		if (u != null) {
			u.setRol_fk(new Rol(null, rol.toUpperCase()));
			usuarioServicio.modificar(id, u);
			redirect.addFlashAttribute("f_mensaje", "Rol de usuario actualizado a " + rol.toUpperCase() + ".");
		}
		return "redirect:/admin/usuarios";
	}

	@PostMapping("/admin/usuarios/borrar/{id}")
	public String borrar(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		usuarioServicio.borrar(id);
		redirect.addFlashAttribute("f_mensaje", "Usuario eliminado del sistema.");
		return "redirect:/admin/usuarios";
	}
}