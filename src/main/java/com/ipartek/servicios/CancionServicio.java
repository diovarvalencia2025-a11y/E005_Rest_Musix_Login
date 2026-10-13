package com.ipartek.servicios;

import java.util.List;

import com.ipartek.pojos.Cancion;

public interface CancionServicio {

	public List<Cancion> obtenerTodas();

	public List<Cancion> obtenerTodasAdmin();

	public Cancion obtenerPorId(Integer id);

	public List<Cancion> obtenerPorUsuario(Integer idUsuario);

	public List<Cancion> buscarFiltro(Integer idUsu, Integer idEst, Integer duracionMin, Integer duracionMax, Boolean reportada);

	public Cancion insertar(Cancion cancion);

	public Cancion modificar(Cancion cancion);

	public Cancion modificar(Integer id, Cancion cancion);

	public Boolean borrar(Integer id);

	public Boolean reportar(Integer id);
}
