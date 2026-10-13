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

import com.ipartek.pojos.Cancion;
import com.ipartek.pojos.Estilo;
import com.ipartek.pojos.Usuario;
import com.ipartek.servicios.CancionServicio;
import com.ipartek.servicios.EstiloServicio;
import com.ipartek.servicios.UsuarioServicio;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminCancionesControlador {

	@Autowired
	private CancionServicio cancionServicio;

	@Autowired
	private EstiloServicio estiloServicio;

	@Autowired
	private UsuarioServicio usuarioServicio;

	@GetMapping("/admin/canciones")
	public String listar(@RequestParam(required = false) String titulo,
			@RequestParam(required = false) Integer duracionMin,
			@RequestParam(required = false) Integer duracionMax,
			@RequestParam(required = false) Integer estilo,
			@RequestParam(required = false) String texto,
			@RequestParam(required = false) Boolean reportada,
			@RequestParam(required = false) Integer usuarioId,
			@RequestParam(defaultValue = "1") Integer page,
			HttpSession session,
			Model model) {

		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		List<Cancion> todas = cancionServicio.obtenerTodasAdmin();
		if (todas == null) todas = List.of();

		List<Estilo> estilos = estiloServicio.obtenerTodos();
		if (estilos == null) estilos = List.of();

		List<Usuario> usuarios = usuarioServicio.obtenerTodos();
		if (usuarios == null) usuarios = List.of();

		long totalCanciones = todas.size();
		long totalReportadas = todas.stream().filter(c -> Boolean.TRUE.equals(c.getReportada())).count();
		long totalEstilos = estilos.size();
		long totalArtistas = todas.stream()
				.filter(c -> c.getUsuario_fk() != null)
				.map(c -> c.getUsuario_fk().getId())
				.distinct()
				.count();

		List<Cancion> filtradas = todas.stream()
				.filter(c -> {
					boolean matchTexto = true;
					if (texto != null && !texto.isBlank()) {
						String query = texto.toLowerCase().trim();
						boolean enTitulo = c.getTitulo() != null && c.getTitulo().toLowerCase().contains(query);
						boolean enArtista = c.getUsuario_fk() != null && c.getUsuario_fk().getUser() != null && c.getUsuario_fk().getUser().toLowerCase().contains(query);
						boolean enEstilo = c.getEstilo_fk() != null && c.getEstilo_fk().getNombre() != null && c.getEstilo_fk().getNombre().toLowerCase().contains(query);
						matchTexto = enTitulo || enArtista || enEstilo;
					}

					boolean matchTitulo = (titulo == null || titulo.isBlank()) ||
							(c.getTitulo() != null && c.getTitulo().toLowerCase().contains(titulo.toLowerCase().trim()));

					boolean matchDurMin = (duracionMin == null) || (c.getDuracion() != null && c.getDuracion() >= duracionMin);
					boolean matchDurMax = (duracionMax == null) || (c.getDuracion() != null && c.getDuracion() <= duracionMax);

					boolean matchEstilo = (estilo == null || estilo == 0) ||
							(c.getEstilo_fk() != null && estilo.equals(c.getEstilo_fk().getId()));

					boolean matchReportada = (reportada == null) || reportada.equals(c.getReportada());

					boolean matchUsuario = (usuarioId == null || usuarioId == 0) ||
							(c.getUsuario_fk() != null && usuarioId.equals(c.getUsuario_fk().getId()));

					return matchTexto && matchTitulo && matchDurMin && matchDurMax && matchEstilo && matchReportada && matchUsuario;
				})
				.collect(Collectors.toList());

		int pageSize = 6;
		int totalItems = filtradas.size();
		int totalPaginas = (int) Math.ceil((double) totalItems / pageSize);
		if (totalPaginas < 1) totalPaginas = 1;
		if (page < 1) page = 1;
		if (page > totalPaginas) page = totalPaginas;

		int fromIndex = (page - 1) * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, totalItems);

		List<Cancion> paginaCanciones = (fromIndex <= toIndex && fromIndex < totalItems)
				? filtradas.subList(fromIndex, toIndex)
				: List.of();

		int mostrandoDesde = (totalItems > 0) ? fromIndex + 1 : 0;
		int mostrandoHasta = toIndex;

		model.addAttribute("canciones", paginaCanciones);
		model.addAttribute("estilos", estilos);
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("usuario", usu);

		model.addAttribute("totalCanciones", totalCanciones);
		model.addAttribute("totalReportadas", totalReportadas);
		model.addAttribute("totalEstilos", totalEstilos);
		model.addAttribute("totalArtistas", totalArtistas);

		model.addAttribute("paginaActual", page);
		model.addAttribute("totalPaginas", totalPaginas);
		model.addAttribute("totalItems", totalItems);
		model.addAttribute("mostrandoDesde", mostrandoDesde);
		model.addAttribute("mostrandoHasta", mostrandoHasta);

		model.addAttribute("fTitulo", titulo);
		model.addAttribute("fDurMin", duracionMin);
		model.addAttribute("fDurMax", duracionMax);
		model.addAttribute("fEstilo", estilo);
		model.addAttribute("fTexto", texto);
		model.addAttribute("fReportada", reportada);
		model.addAttribute("fUsuarioId", usuarioId);

		return "plantillas/admin-canciones";
	}

	@PostMapping("/admin/canciones/aprobar/{id}")
	public String aprobar(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		Cancion c = cancionServicio.obtenerPorId(id);
		if (c != null) {
			c.setReportada(false);
			cancionServicio.modificar(c);
			redirect.addFlashAttribute("f_mensaje", "Canción '" + c.getTitulo() + "' aprobada y desbaneada con éxito.");
		} else {
			redirect.addFlashAttribute("f_error", "No se encontró la canción");
		}
		return "redirect:/admin/canciones";
	}

	@PostMapping("/admin/canciones/reportar/{id}")
	public String reportarAdmin(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		cancionServicio.reportar(id);
		redirect.addFlashAttribute("f_mensaje", "Canción marcada como reportada/baneada.");
		return "redirect:/admin/canciones";
	}

	// Banear canción del catálogo público
	@org.springframework.web.bind.annotation.RequestMapping(value = "/admin/canciones/banear/{id}", method = {org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.GET})
	public String banearCancion(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		Cancion c = cancionServicio.obtenerPorId(id);
		if (c != null) {
			c.setReportada(true);
			cancionServicio.modificar(c);
			redirect.addFlashAttribute("f_mensaje", "Canción '" + c.getTitulo() + "' baneada del catálogo.");
		} else {
			redirect.addFlashAttribute("f_error", "No se encontró la canción");
		}
		return "redirect:/admin/canciones";
	}

	@PostMapping("/admin/canciones/borrar/{id}")
	public String borrar(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		cancionServicio.borrar(id);
		redirect.addFlashAttribute("f_mensaje", "Canción eliminada del catálogo por moderación.");
		return "redirect:/admin/canciones";
	}

	@PostMapping("/admin/canciones/banear-usuario/{usuarioId}")
	public String banearUsuarioDesdeCancion(@PathVariable Integer usuarioId, HttpSession session, RedirectAttributes redirect) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null || usu.getRol_fk() == null || !"admin".equalsIgnoreCase(usu.getRol_fk().getNombre())) {
			return "redirect:/login";
		}

		usuarioServicio.banear(usuarioId);
		redirect.addFlashAttribute("f_mensaje", "Usuario de la canción baneado con éxito.");
		return "redirect:/admin/canciones";
	}
}