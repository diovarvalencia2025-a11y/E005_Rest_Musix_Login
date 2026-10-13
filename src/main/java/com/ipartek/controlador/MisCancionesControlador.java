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

import jakarta.servlet.http.HttpSession;

@Controller
public class MisCancionesControlador {

	@Autowired
	private CancionServicio cancionServicio;

	@Autowired
	private EstiloServicio estiloServicio;

	private boolean validarSesion(HttpSession session) {
		Usuario usu = (Usuario) session.getAttribute("usuario");
		return usu != null;
	}

	@GetMapping("/mis-canciones")
	public String listar(@RequestParam(required = false) String texto,
			@RequestParam(required = false) Integer estilo,
			@RequestParam(defaultValue = "1") Integer page,
			HttpSession session,
			Model model,
			RedirectAttributes redirect) {

		if (!validarSesion(session)) {
			redirect.addFlashAttribute("error", "Debes iniciar sesión para acceder a tu música.");
			return "redirect:/login";
		}

		Usuario usu = (Usuario) session.getAttribute("usuario");
		List<Cancion> misCanciones = cancionServicio.obtenerPorUsuario(usu.getId());
		if (misCanciones == null) misCanciones = List.of();

		List<Estilo> estilos = estiloServicio.obtenerTodos();
		if (estilos == null) estilos = List.of();

		long totalMisCanciones = misCanciones.size();
		long totalSegundos = misCanciones.stream().mapToLong(c -> c.getDuracion() != null ? c.getDuracion() : 0).sum();
		long totalMinutos = totalSegundos / 60;
		long totalEstilos = misCanciones.stream()
				.filter(c -> c.getEstilo_fk() != null)
				.map(c -> c.getEstilo_fk().getId())
				.distinct()
				.count();
		long totalReportadas = misCanciones.stream().filter(c -> Boolean.TRUE.equals(c.getReportada())).count();

		List<Cancion> filtradas = misCanciones.stream()
				.filter(c -> {
					boolean matchTexto = true;
					if (texto != null && !texto.isBlank()) {
						String q = texto.toLowerCase().trim();
						boolean enTit = c.getTitulo() != null && c.getTitulo().toLowerCase().contains(q);
						boolean enDesc = c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(q);
						matchTexto = enTit || enDesc;
					}

					boolean matchEstilo = (estilo == null || estilo == 0) ||
							(c.getEstilo_fk() != null && estilo.equals(c.getEstilo_fk().getId()));

					return matchTexto && matchEstilo;
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
		model.addAttribute("usuario", usu);

		model.addAttribute("totalMisCanciones", totalMisCanciones);
		model.addAttribute("totalMinutos", totalMinutos);
		model.addAttribute("totalEstilos", totalEstilos);
		model.addAttribute("totalReportadas", totalReportadas);

		model.addAttribute("paginaActual", page);
		model.addAttribute("totalPaginas", totalPaginas);
		model.addAttribute("totalItems", totalItems);
		model.addAttribute("mostrandoDesde", mostrandoDesde);
		model.addAttribute("mostrandoHasta", mostrandoHasta);

		model.addAttribute("fTexto", texto);
		model.addAttribute("fEstilo", estilo);

		return "plantillas/mis-canciones";
	}

	@GetMapping("/mis-canciones/nueva")
	public String formularioNueva(HttpSession session, Model model, RedirectAttributes redirect) {
		if (!validarSesion(session)) {
			redirect.addFlashAttribute("error", "Debes iniciar sesión para añadir canciones.");
			return "redirect:/login";
		}

		Usuario usu = (Usuario) session.getAttribute("usuario");
		List<Estilo> estilos = estiloServicio.obtenerTodos();
		model.addAttribute("estilos", estilos);
		model.addAttribute("cancion", new Cancion());
		model.addAttribute("usuario", usu);

		return "plantillas/form-cancion";
	}

	@PostMapping("/mis-canciones/nueva")
	public String guardarNueva(@RequestParam String titulo,
			@RequestParam String enlace,
			@RequestParam Integer duracion,
			@RequestParam Integer estiloId,
			@RequestParam(required = false) String descripcion,
			HttpSession session,
			RedirectAttributes redirect) {

		if (!validarSesion(session)) {
			return "redirect:/login";
		}

		Usuario usu = (Usuario) session.getAttribute("usuario");

		Cancion cancion = new Cancion();
		cancion.setTitulo(titulo);
		cancion.setEnlace(enlace);
		cancion.setDuracion(duracion);
		cancion.setDescripcion(descripcion);
		cancion.setReportada(false);
		cancion.setUsuario_fk(usu);

		Estilo estilo = new Estilo();
		estilo.setId(estiloId);
		cancion.setEstilo_fk(estilo);

		cancionServicio.insertar(cancion);
		redirect.addFlashAttribute("f_mensaje", "Canción guardada con éxito.");

		return "redirect:/mis-canciones";
	}

	@GetMapping("/mis-canciones/editar/{id}")
	public String formularioEditar(@PathVariable Integer id,
			HttpSession session,
			Model model,
			RedirectAttributes redirect) {

		if (!validarSesion(session)) {
			return "redirect:/login";
		}

		Usuario usu = (Usuario) session.getAttribute("usuario");
		Cancion cancion = cancionServicio.obtenerPorId(id);
		List<Estilo> estilos = estiloServicio.obtenerTodos();

		model.addAttribute("cancion", cancion);
		model.addAttribute("estilos", estilos);
		model.addAttribute("usuario", usu);

		return "plantillas/form-cancion";
	}

	@PostMapping("/mis-canciones/editar/{id}")
	public String guardarEdicion(@PathVariable Integer id,
			@RequestParam String titulo,
			@RequestParam String enlace,
			@RequestParam Integer duracion,
			@RequestParam Integer estiloId,
			@RequestParam(required = false) String descripcion,
			HttpSession session,
			RedirectAttributes redirect) {

		if (!validarSesion(session)) {
			return "redirect:/login";
		}

		Cancion cancion = cancionServicio.obtenerPorId(id);
		cancion.setTitulo(titulo);
		cancion.setEnlace(enlace);
		cancion.setDuracion(duracion);
		cancion.setDescripcion(descripcion);

		Estilo estilo = new Estilo();
		estilo.setId(estiloId);
		cancion.setEstilo_fk(estilo);

		cancionServicio.modificar(cancion);
		redirect.addFlashAttribute("f_mensaje", "Canción actualizada con éxito.");

		return "redirect:/mis-canciones";
	}

	@PostMapping("/mis-canciones/borrar/{id}")
	public String borrar(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
		if (!validarSesion(session)) {
			return "redirect:/login";
		}

		cancionServicio.borrar(id);
		redirect.addFlashAttribute("f_mensaje", "Canción eliminada correctamente.");

		return "redirect:/mis-canciones";
	}
}
