package com.ipartek.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class CancionesControlador {

	@Autowired
	private CancionServicio cancionServicio;

	@Autowired
	private EstiloServicio estiloServicio;

	@GetMapping("/canciones")
	public String listarCanciones(@RequestParam(required = false) String texto,
			@RequestParam(required = false) Integer estilo,
			@RequestParam(required = false) Integer duracionMin,
			@RequestParam(required = false) Integer duracionMax,
			@RequestParam(defaultValue = "1") Integer page,
			HttpSession session,
			Model model) {

		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null) {
			return "redirect:/login";
		}

		List<Cancion> todas = cancionServicio.obtenerTodas();
		if (todas == null) todas = List.of();

		List<Estilo> estilos = estiloServicio.obtenerTodos();
		if (estilos == null) estilos = List.of();

		// 1. MÉTRICAS KPIS
		long totalCanciones = todas.size();
		long totalSegundos = todas.stream().mapToLong(c -> c.getDuracion() != null ? c.getDuracion() : 0).sum();
		long totalMinutos = totalSegundos / 60;
		long totalGeneros = estilos.size();
		long totalArtistas = todas.stream()
				.filter(c -> c.getUsuario_fk() != null)
				.map(c -> c.getUsuario_fk().getId())
				.distinct()
				.count();

		// 2. CONTEO DE CANCIONES POR ESTILO
		Map<Integer, Long> conteoPorEstilo = new HashMap<>();
		for (Estilo est : estilos) {
			long count = todas.stream()
					.filter(c -> c.getEstilo_fk() != null && est.getId().equals(c.getEstilo_fk().getId()))
					.count();
			conteoPorEstilo.put(est.getId(), count);
		}

		// 3. FILTRADO
		List<Cancion> filtradas = todas.stream()
				.filter(c -> {
					boolean matchTexto = true;
					if (texto != null && !texto.isBlank()) {
						String query = texto.toLowerCase().trim();
						boolean enTitulo = c.getTitulo() != null && c.getTitulo().toLowerCase().contains(query);
						boolean enArtista = c.getUsuario_fk() != null && c.getUsuario_fk().getUser() != null && c.getUsuario_fk().getUser().toLowerCase().contains(query);
						boolean enEstilo = c.getEstilo_fk() != null && c.getEstilo_fk().getNombre() != null && c.getEstilo_fk().getNombre().toLowerCase().contains(query);
						boolean enDesc = c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(query);
						matchTexto = enTitulo || enArtista || enEstilo || enDesc;
					}

					boolean matchEstilo = (estilo == null || estilo == 0) ||
							(c.getEstilo_fk() != null && estilo.equals(c.getEstilo_fk().getId()));

					boolean matchDurMin = (duracionMin == null) || (c.getDuracion() != null && c.getDuracion() >= duracionMin);
					boolean matchDurMax = (duracionMax == null) || (c.getDuracion() != null && c.getDuracion() <= duracionMax);

					return matchTexto && matchEstilo && matchDurMin && matchDurMax;
				})
				.collect(Collectors.toList());

		// 4. PAGINACIÓN
		int pageSize = 8;
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

		// Model attributes
		model.addAttribute("canciones", paginaCanciones);
		model.addAttribute("estilos", estilos);
		model.addAttribute("usuario", usu);
		model.addAttribute("conteoPorEstilo", conteoPorEstilo);

		model.addAttribute("totalCanciones", totalCanciones);
		model.addAttribute("totalMinutos", totalMinutos);
		model.addAttribute("totalGeneros", totalGeneros);
		model.addAttribute("totalArtistas", totalArtistas);

		model.addAttribute("paginaActual", page);
		model.addAttribute("totalPaginas", totalPaginas);
		model.addAttribute("totalItems", totalItems);
		model.addAttribute("mostrandoDesde", mostrandoDesde);
		model.addAttribute("mostrandoHasta", mostrandoHasta);

		model.addAttribute("fTexto", texto);
		model.addAttribute("fEstilo", estilo);
		model.addAttribute("fDurMin", duracionMin);
		model.addAttribute("fDurMax", duracionMax);

		return "plantillas/canciones";
	}

	@PostMapping("/canciones/{id}/reportar")
	public String reportarCancion(@PathVariable Integer id,
			HttpSession session,
			RedirectAttributes redirect) {

		Usuario usu = (Usuario) session.getAttribute("usuario");
		if (usu == null) {
			return "redirect:/login";
		}

		cancionServicio.reportar(id);
		redirect.addFlashAttribute("f_mensaje", "Canción reportada a moderación con éxito.");
		return "redirect:/canciones";
	}
}