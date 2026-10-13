package com.ipartek.servicios;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ipartek.pojos.Usuario;

public interface UsuarioServicio {

	public List<Usuario> obtenerTodos();

	public Usuario obtenerPorId(Integer id);

	public Usuario obtenerPorUser(String user);

	public Usuario insertar(Usuario usuario);

	public Usuario modificar(Integer id, Usuario usuario);

	public Boolean borrar(Integer id);

	public Usuario validarLogin(String user, String pass);

	public void resetearIntentos(Integer id);

	public void cambiarPassword(Integer id, String nuevoPass);

	public void banear(Integer id);

	public void desbanear(Integer id);

	public String subirFoto(MultipartFile archivo);
}
