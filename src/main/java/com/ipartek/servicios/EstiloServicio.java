package com.ipartek.servicios;

import java.util.List;

import com.ipartek.pojos.Estilo;

public interface EstiloServicio {

	public List<Estilo> obtenerTodos();

	public Estilo obtenerPorId(Integer id);
}
