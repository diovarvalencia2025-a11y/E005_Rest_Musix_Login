package com.ipartek.controlador;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleMaxSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("error",
				"La imagen seleccionada supera el tamaño máximo permitido. Por favor, selecciona una foto más pequeña.");
		String referer = request.getHeader("Referer");
		if (referer != null && !referer.isBlank()) {
			return "redirect:" + referer;
		}
		return "redirect:/registro";
	}
}
