package com.ipartek.componentes;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component

public class JwtUtil {
	
	
	@Value(value = "${super.secret.key:clave_super_secreta_jwt_musix_para_firmar_tokens_1234567890}")
	private String SECRET_KEY;

	public String generateToken(String username, String role) {
		return Jwts.builder()
			.setSubject(username)
			.claim("rol", role)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000 * 24)) // 24 horas
			.signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
			.compact();
	}
	 
	public Claims extractClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
	 	
	public boolean isTokenValid(String token, String ip) {
	    try {
	        Claims claims = extractClaims(token);
	        
	        boolean fecha= claims.getExpiration().after(new Date());
	      
	        
	        
	        
	        return (fecha);
	    } catch (Exception e) {
	        return false;
	    }
	}



	
	
	



}
