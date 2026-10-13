package com.ipartek.servicios;

import com.ipartek.pojos.PreguntasUsuario;

public interface PreguntasUsuarioServicio {

	public void insertar(PreguntasUsuario pu);

	public boolean validarRespuestas(String username, String r1, String r2, String r3);
}
