document.addEventListener('DOMContentLoaded', function() {
    const cajaMensajes = document.getElementById('cajaMensajes');
    
    // Solo si existe y tiene contenido
    if (cajaMensajes && cajaMensajes.textContent.trim() !== '') {
        // Esperar 5 segundos y ocultar
        setTimeout(() => {
            cajaMensajes.style.display = 'none';
        }, 5000);
    }
});