package com.ipartek.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
	private Integer id;
	private String user;
	private String pass;
	private String salt;
	private Rol rol_fk;
	private String foto;
	private Integer intentos;

	public boolean isBaneado() {
		if (rol_fk != null) {
			if (rol_fk.getId() != null && (rol_fk.getId() == 4 || rol_fk.getId() == 3)) {
				return true;
			}
			if (rol_fk.getNombre() != null && 
				("BANEADO".equalsIgnoreCase(rol_fk.getNombre()) || "BLOQUEADO".equalsIgnoreCase(rol_fk.getNombre()))) {
				return true;
			}
		}
		return intentos != null && intentos >= 3;
	}

	public boolean getBaneado() {
		return isBaneado();
	}

	public String getRol() {
		return (rol_fk != null && rol_fk.getNombre() != null) ? rol_fk.getNombre() : "USER";
	}
}