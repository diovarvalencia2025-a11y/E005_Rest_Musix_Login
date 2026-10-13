function calcularSha256() {
    const texto = document.getElementById('pass_sin_hash').value;
    
    if (texto === '') {
        document.getElementById('pass').value = '';
        return;
    }

    // Calcular el hash SHA-256 usando CryptoJS
    const hash = CryptoJS.SHA256(texto + 'Galea'); // pimienta
    
    // Convertir a hexadecimal y asignar al segundo input
    document.getElementById('pass').value = hash.toString(CryptoJS.enc.Hex);
}

//function calcularSha256Pregunta(num) {
//    const texto = document.getElementById('respuesta'+num).value;
//    
//    if (texto === '') {
//        document.getElementById('hash_respuesta'+num).value = '';
//        return;
//    }
//
//    // Calcular el hash SHA-256 usando CryptoJS
//    const hash = CryptoJS.SHA256(texto + 'Galea'); // pimienta
//    
//    // Convertir a hexadecimal y asignar al segundo input
//    document.getElementById('hash_respuesta'+num).value = hash.toString(CryptoJS.enc.Hex);
//}