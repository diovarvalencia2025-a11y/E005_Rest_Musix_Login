/**
 * MUSIX - Controlador de Menú Lateral y Drawer Responsivo
 * Permite abrir y cerrar el menú lateral en dispositivos móviles y escritorios estrechos.
 */
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    if (!sidebar) return;
    
    const isOpen = sidebar.classList.toggle('abierto');
    if (overlay) {
        overlay.classList.toggle('activo', isOpen);
    }
    document.body.classList.toggle('sidebar-abierto', isOpen);
}

// Cerrar con Escape
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        const sidebar = document.querySelector('.sidebar');
        const overlay = document.querySelector('.sidebar-overlay');
        if (sidebar && sidebar.classList.contains('abierto')) {
            sidebar.classList.remove('abierto');
            if (overlay) overlay.classList.remove('activo');
            document.body.classList.remove('sidebar-abierto');
        }
    }
});

// Cerrar al pulsar un enlace de navegación en vista móvil
document.addEventListener('DOMContentLoaded', function () {
    const items = document.querySelectorAll('.sidebar .menu-item');
    items.forEach(function (item) {
        item.addEventListener('click', function () {
            if (window.innerWidth <= 992) {
                const sidebar = document.querySelector('.sidebar');
                const overlay = document.querySelector('.sidebar-overlay');
                if (sidebar) sidebar.classList.remove('abierto');
                if (overlay) overlay.classList.remove('activo');
                document.body.classList.remove('sidebar-abierto');
            }
        });
    });
});

// Limpiar estado al redimensionar la ventana a tamaño de escritorio
window.addEventListener('resize', function () {
    if (window.innerWidth > 992) {
        const sidebar = document.querySelector('.sidebar');
        const overlay = document.querySelector('.sidebar-overlay');
        if (sidebar) sidebar.classList.remove('abierto');
        if (overlay) overlay.classList.remove('activo');
        document.body.classList.remove('sidebar-abierto');
    }
});
