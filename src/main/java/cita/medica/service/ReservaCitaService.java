package cita.medica.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import cita.medica.dto.CrearCitaDTO;
import cita.medica.dto.ReservaCitaDTO;

@Service

public class ReservaCitaService {

    /**Horarios medico*/
    private static final List<LocalTime> BLOQUES_HORARIOS = List.of(
            LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30),
            LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(12, 0), LocalTime.of(12, 30),
            LocalTime.of(13, 0), LocalTime.of(13, 30), LocalTime.of(14, 0), LocalTime.of(14, 30),
            LocalTime.of(15, 0), LocalTime.of(15, 30), LocalTime.of(16, 0), LocalTime.of(16, 30),
            LocalTime.of(17, 0));
     
    private final List<ReservaCitaDTO> reservas = new ArrayList<ReservaCitaDTO>();

    /**Datos iniciales */
    public ReservaCitaService(){

        reservas.add(ReservaCitaDTO.builder().id(1L).nombreMedico("Javier Garcia").fecha(LocalDate.of(2026, 3, 25)).hora(LocalTime.of(10, 0)).especialidad("Oftalmologia").estado("Reservada").build());

        reservas.add(ReservaCitaDTO.builder().id(2L).nombreMedico("Nicolas Arrue").fecha(LocalDate.of(2026, 3, 25)).hora(LocalTime.of(15, 30)).especialidad("Medicina General").estado("Reservada").build());

        reservas.add(ReservaCitaDTO.builder().id(3L).nombreMedico("Martin Jhon").fecha(LocalDate.of(2026, 3, 2)).hora(LocalTime.of(13, 30)).especialidad("Toma de Muestras").estado("Reservada").build());

    }

    /**Muestra las ordenes*/
    public List<ReservaCitaDTO> obtenerTodas() {

        return reservas;
    }

    /**Buscar por id*/
    public ReservaCitaDTO obtenerPorId(Long id) {

        for (ReservaCitaDTO reserva : reservas) {

            if (reserva.getId().equals(id)){

                return reserva;
            }
        }
        return null;
    }

    /**Crear reserva luego de validar hora */
    public ReservaCitaDTO crear(CrearCitaDTO request) {
        validarHorarioPermitido(request.getHora());
        validarConflicto(null, request);

        ReservaCitaDTO nuevaReserva = ReservaCitaDTO.builder().id(siguienteId())
                .nombreMedico(request.getNombreMedico()).fecha(request.getFecha()).hora(request.getHora())
                .especialidad(request.getEspecialidad()).estado("Reservada").build();

        reservas.add(nuevaReserva);
        return nuevaReserva;
    }

    /**Actualizacion */
    public ReservaCitaDTO actualizar(Long id, CrearCitaDTO request) {

        ReservaCitaDTO reserva = obtenerPorId(id);

        if (reserva == null) {
            return null;
        }

        validarHorarioPermitido(request.getHora());
        validarConflicto(id, request);

        reserva.setNombreMedico(request.getNombreMedico());
        reserva.setFecha(request.getFecha());
        reserva.setHora(request.getHora());
        reserva.setEspecialidad(request.getEspecialidad());

        return reserva;
    }

    //**Cambio de estado de reserva */
    public ReservaCitaDTO cancelar(Long id) {

        for (ReservaCitaDTO reserva : reservas) {

            if (reserva.getId().equals(id)) {

                reserva.setEstado("Anulada");
                return reserva;
            }
        }
        return null;
    }

    /**id autoincremental */
    private Long siguienteId() {

        long maxId = 0L;

        for (ReservaCitaDTO reserva : reservas) {
            if (reserva.getId() != null && reserva.getId() > maxId) {
                maxId = reserva.getId();
            }
        }

        return maxId + 1;
    }

    /**Evita tomar nuevamente la misma hora */
    private void validarConflicto(Long reservaIdIgnorada, CrearCitaDTO request) {

        for (ReservaCitaDTO reserva : reservas) {

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

    /**Validador de hora */
    private void validarHorarioPermitido(LocalTime hora) {

        if (!BLOQUES_HORARIOS.contains(hora)) {
            throw new IllegalArgumentException("La hora debe coincidir con uno de los bloques disponibles del sistema");
        }
    }

    /**Valida horas medicas quitando las tomadas */
    public Map<String, Object> consultarDisponibilidad(LocalDate fecha, String nombreMedico, String especialidad) {

        Set<LocalTime> horariosOcupados = new HashSet<>();
        List<LocalTime> horariosDisponibles = new ArrayList<>();

        for (ReservaCitaDTO reserva : reservas) {

            boolean mismaFecha = reserva.getFecha().equals(fecha);
            boolean mismoMedico = reserva.getNombreMedico().equals(nombreMedico);
            boolean mismaEspecialidad = reserva.getEspecialidad().equals(especialidad);
            boolean reservaActiva = !"Anulada".equals(reserva.getEstado());

            if (mismaFecha && mismoMedico && mismaEspecialidad && reservaActiva){
                horariosOcupados.add(reserva.getHora());
            }
        }

        for (LocalTime bloque : BLOQUES_HORARIOS) {
            if (!horariosOcupados.contains(bloque)) {
                horariosDisponibles.add(bloque);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("fecha", fecha);
        response.put("nombreMedico", nombreMedico);
        response.put("especialidad", especialidad);
        response.put("horariosDisponibles", horariosDisponibles);

        return response;
    }
}
