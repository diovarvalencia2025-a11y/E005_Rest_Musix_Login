package com.ipartek.servicios;

import java.util.List;

import com.ipartek.pojos.Pregunta;

public interface PreguntaServicio {

	public List<Pregunta> obtenerTodas();

	public Pregunta obtenerPorId(Integer id);
}
