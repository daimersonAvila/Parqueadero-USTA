// Comportamiento común a todas las pantallas

// Contador "Actualizado hace X segundos" de la página principal
document.querySelectorAll('[data-updated-at]').forEach(function (chip) {
    var updatedAt = Number(chip.dataset.updatedAt);
    var text = chip.querySelector('.live-text');
    if (!updatedAt || !text) {
        return;
    }

    function refresh() {
        var seconds = Math.max(0, Math.floor((Date.now() - updatedAt) / 1000));
        if (seconds < 60) {
            text.textContent = 'Actualizado hace ' + seconds + (seconds === 1 ? ' segundo' : ' segundos');
        } else {
            text.textContent = 'Actualizado hace ' + Math.floor(seconds / 60) + ' min';
        }
    }

    refresh();
    setInterval(refresh, 1000);
});
