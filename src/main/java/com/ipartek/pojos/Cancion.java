package com.ipartek.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cancion {
	private Integer id;
	private String titulo;
	private String enlace;
	private Integer duracion;
	private Estilo estilo_fk;
	private String descripcion;
	private Boolean reportada;
	private Usuario usuario_fk;

	public Estilo getEstilo() {
		return estilo_fk;
	}

	public void setEstilo(Estilo estilo) {
		this.estilo_fk = estilo;
	}

	public Usuario getUsuario() {
		return usuario_fk;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario_fk = usuario;
	}
}
