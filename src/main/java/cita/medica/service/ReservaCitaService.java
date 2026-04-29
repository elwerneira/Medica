package cita.medica.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cita.medica.dto.CrearCitaDTO;
import cita.medica.dto.ReservaCitaDTO;
import cita.medica.entity.CitaEntity;
import cita.medica.repository.CitaRepository;

@Service
public class ReservaCitaService {

    private static final List<LocalTime> BLOQUES_HORARIOS = List.of(
            LocalTime.of(8, 0),
            LocalTime.of(8, 30),
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            LocalTime.of(10, 0),
            LocalTime.of(10, 30),
            LocalTime.of(11, 0),
            LocalTime.of(11, 30),
            LocalTime.of(12, 0),
            LocalTime.of(12, 30),
            LocalTime.of(14, 0),
            LocalTime.of(14, 30),
            LocalTime.of(15, 0),
            LocalTime.of(15, 30),
            LocalTime.of(16, 0),
            LocalTime.of(16, 30),
            LocalTime.of(17, 0),
            LocalTime.of(17, 30));

    private final CitaRepository repository;

    public ReservaCitaService(CitaRepository repository) {
        this.repository = repository;
    }

    public List<ReservaCitaDTO> obtenerTodas() {
        return repository.findAllByOrderByIdAsc().stream()
                .map(this::toDto)
                .toList();
    }

    public ReservaCitaDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public ReservaCitaDTO crear(CrearCitaDTO request) {
        validarHorarioPermitido(request.getHora());
        validarConflicto(null, request);

        CitaEntity nuevaReserva = CitaEntity.builder()
                .nombreMedico(request.getNombreMedico())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .especialidad(request.getEspecialidad())
                .estado("Reservada")
                .build();

        return toDto(repository.save(nuevaReserva));
    }

    public ReservaCitaDTO actualizar(Long id, CrearCitaDTO request) {
        CitaEntity reserva = repository.findById(id).orElse(null);

        if (reserva == null) {
            return null;
        }

        validarHorarioPermitido(request.getHora());
        validarConflicto(id, request);

        reserva.setNombreMedico(request.getNombreMedico());
        reserva.setFecha(request.getFecha());
        reserva.setHora(request.getHora());
        reserva.setEspecialidad(request.getEspecialidad());

        return toDto(repository.save(reserva));
    }

    public ReservaCitaDTO cancelar(Long id) {
        CitaEntity reserva = repository.findById(id).orElse(null);

        if (reserva == null) {
            return null;
        }

        reserva.setEstado("Anulada");
        return toDto(repository.save(reserva));
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    private void validarConflicto(Long reservaIdIgnorada, CrearCitaDTO request) {
        List<CitaEntity> reservas = repository.findAllByOrderByIdAsc();

        for (CitaEntity reserva : reservas) {
            boolean mismaReserva = reservaIdIgnorada != null && reserva.getId().equals(reservaIdIgnorada);
            boolean mismaFecha = reserva.getFecha().equals(request.getFecha());
            boolean mismaHora = reserva.getHora().equals(request.getHora());
            boolean mismoMedico = reserva.getNombreMedico().equals(request.getNombreMedico());
            boolean reservaActiva = !"Anulada".equals(reserva.getEstado());

            if (!mismaReserva && mismaFecha && mismaHora && mismoMedico && reservaActiva) {
                throw new IllegalArgumentException("El medico ya tiene una cita reservada en ese horario");
            }
        }
    }

    private void validarHorarioPermitido(LocalTime hora) {
        if (!BLOQUES_HORARIOS.contains(hora)) {
            throw new IllegalArgumentException("La hora debe coincidir con uno de los bloques disponibles del sistema");
        }
    }

    public Map<String, Object> consultarDisponibilidad(LocalDate fecha, String nombreMedico, String especialidad) {
        List<LocalTime> horariosOcupados = repository
                .findByFechaAndNombreMedicoAndEspecialidadAndEstadoNot(fecha, nombreMedico, especialidad, "Anulada")
                .stream()
                .map(CitaEntity::getHora)
                .toList();

        List<LocalTime> horariosDisponibles = BLOQUES_HORARIOS.stream()
                .filter(bloque -> !horariosOcupados.contains(bloque))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("fecha", fecha);
        response.put("nombreMedico", nombreMedico);
        response.put("especialidad", especialidad);
        response.put("horariosDisponibles", horariosDisponibles);

        return response;
    }

    private ReservaCitaDTO toDto(CitaEntity entity) {
        return ReservaCitaDTO.builder()
                .id(entity.getId())
                .nombreMedico(entity.getNombreMedico())
                .fecha(entity.getFecha())
                .hora(entity.getHora())
                .especialidad(entity.getEspecialidad())
                .estado(entity.getEstado())
                .build();
    }
}
