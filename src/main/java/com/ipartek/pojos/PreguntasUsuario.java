package com.ipartek.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntasUsuario {
	
	private Integer id;
	private Usuario usuario_fk;
	private Pregunta pregunta_fk;
	private String respuesta;

}
