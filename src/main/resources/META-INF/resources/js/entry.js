// Pantalla "Registrar entrada": panel de cupos de la zona y campos del vehículo nuevo

(function () {
    var plate = document.getElementById('plate');
    var zoneSelect = document.getElementById('zoneId');
    var newVehicle = document.getElementById('newVehicle');
    var newVehicleFields = document.getElementById('newVehicleFields');

    var panelEmpty = document.getElementById('panelEmpty');
    var panelContent = document.getElementById('panelContent');
    var panelFree = document.getElementById('panelFree');
    var panelFreeLabel = document.getElementById('panelFreeLabel');
    var panelPill = document.getElementById('panelPill');
    var panelBar = document.getElementById('panelBar');
    var otherZones = document.getElementById('otherZones');
    var panelHint = document.getElementById('panelHint');
    var panelHintText = document.getElementById('panelHintText');

    // La placa se escribe siempre en mayúsculas
    plate.addEventListener('input', function () {
        plate.value = plate.value.toUpperCase();
    });

    // Los datos del vehículo solo se piden (y se envían) cuando es nuevo
    function toggleNewVehicle() {
        var show = newVehicle.checked;
        newVehicleFields.hidden = !show;
        newVehicleFields.querySelectorAll('input, select').forEach(function (field) {
            field.disabled = !show;
        });
    }

    // Lee los datos de una opción de la lista de zonas
    function readZone(option) {
        return {
            id: option.value,
            name: option.dataset.name,
            type: option.dataset.type,
            free: Number(option.dataset.free),
            freeLabel: option.dataset.freeLabel,
            percent: Number(option.dataset.percent),
            status: option.dataset.status,
            statusLabel: option.dataset.statusLabel
        };
    }

    function allZones() {
        return Array.prototype.slice.call(zoneSelect.options)
            .filter(function (option) { return option.value !== ''; })
            .map(readZone);
    }

    // Zona con más cupos libres del mismo tipo de vehículo (para la sugerencia)
    function bestAlternative(selected, zones) {
        var candidates = zones.filter(function (zone) {
            return zone.id !== selected.id && zone.type === selected.type && zone.free > 0;
        });
        candidates.sort(function (a, b) { return b.free - a.free; });
        return candidates.length > 0 ? candidates[0] : null;
    }

    function updatePanel() {
        var option = zoneSelect.options[zoneSelect.selectedIndex];
        if (!option || option.value === '') {
            panelEmpty.hidden = false;
            panelContent.hidden = true;
            return;
        }

        var zones = allZones();
        var selected = readZone(option);

        panelEmpty.hidden = true;
        panelContent.hidden = false;

        panelFree.textContent = selected.free;
        panelFree.className = 'panel-free text-status-' + selected.status;
        panelFreeLabel.textContent = selected.freeLabel;
        panelPill.textContent = selected.statusLabel;
        panelPill.className = 'status-pill pill-' + selected.status;
        panelBar.style.width = selected.percent + '%';
        panelBar.className = 'progress-bar bar-' + selected.status;

        // Lista de las otras zonas con su color de estado
        otherZones.innerHTML = '';
        zones.filter(function (zone) { return zone.id !== selected.id; }).forEach(function (zone) {
            var item = document.createElement('li');

            var name = document.createElement('span');
            var dot = document.createElement('span');
            dot.className = 'dot dot-' + zone.status;
            name.appendChild(dot);
            name.appendChild(document.createTextNode(' ' + zone.name));

            var free = document.createElement('strong');
            free.textContent = zone.free;

            item.appendChild(name);
            item.appendChild(free);
            otherZones.appendChild(item);
        });

        // Sugerencia cuando la zona elegida no tiene cupos
        if (selected.free === 0) {
            var alternative = bestAlternative(selected, zones);
            panelHint.hidden = false;
            panelHintText.textContent = alternative
                ? 'Sugerencia: dirige al conductor a la ' + alternative.name
                : 'No quedan cupos libres para este tipo de vehículo.';
        } else {
            panelHint.hidden = true;
        }
    }

    newVehicle.addEventListener('change', toggleNewVehicle);
    zoneSelect.addEventListener('change', updatePanel);

    toggleNewVehicle();
    updatePanel();
})();
