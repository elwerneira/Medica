package cita.medica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cita.medica.dto.CrearCitaDTO;
import cita.medica.dto.ReservaCitaDTO;

class ReservaCitaServiceTest {

    private ReservaCitaService service;

    @BeforeEach
    void setUp() {
        service = new ReservaCitaService();
    }

    @Test
    void debeListarReservasIniciales() {
        List<ReservaCitaDTO> reservas = service.obtenerTodas();

        assertEquals(3, reservas.size());
    }

    @Test
    void debeCrearUnaReservaValida() {
        CrearCitaDTO request = CrearCitaDTO.builder()
                .nombreMedico("Andrea Soto")
                .fecha(LocalDate.of(2026, 4, 1))
                .hora(LocalTime.of(9, 30))
                .especialidad("Cardiologia")
                .build();

        ReservaCitaDTO creada = service.crear(request);

        assertEquals(4L, creada.getId());
        assertEquals("Andrea Soto", creada.getNombreMedico());
        assertEquals(LocalDate.of(2026, 4, 1), creada.getFecha());
        assertEquals(LocalTime.of(9, 30), creada.getHora());
        assertEquals("Cardiologia", creada.getEspecialidad());
        assertEquals("Reservada", creada.getEstado());
    }

    @Test
    void debeCancelarUnaReservaExistente() {
        ReservaCitaDTO cancelada = service.cancelar(1L);

        assertEquals(1L, cancelada.getId());
        assertEquals("Anulada", cancelada.getEstado());
    }

    @Test
    void debeInformarHorariosDisponiblesSinIncluirLosOcupados() {
        Map<String, Object> disponibilidad = service.consultarDisponibilidad(
                LocalDate.of(2026, 3, 25),
                "Javier Garcia",
                "Oftalmologia");

        @SuppressWarnings("unchecked")
        List<LocalTime> horarios = (List<LocalTime>) disponibilidad.get("horariosDisponibles");

        assertTrue(horarios.contains(LocalTime.of(9, 0)));
        assertFalse(horarios.contains(LocalTime.of(10, 0)));
    }

    @Test
    void debeRechazarConflictosDeHorarioParaElMismoMedico() {
        CrearCitaDTO request = CrearCitaDTO.builder()
                .nombreMedico("Javier Garcia")
                .fecha(LocalDate.of(2026, 3, 25))
                .hora(LocalTime.of(10, 0))
                .especialidad("Oftalmologia")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.crear(request));

        assertEquals("El medico ya tiene una cita reservada en ese horario", exception.getMessage());
    }

    @Test
    void debeRechazarHorariosFueraDeLosBloquesPermitidos() {
        CrearCitaDTO request = CrearCitaDTO.builder()
                .nombreMedico("Andrea Soto")
                .fecha(LocalDate.of(2026, 4, 1))
                .hora(LocalTime.of(9, 45))
                .especialidad("Cardiologia")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.crear(request));

        assertEquals("La hora debe coincidir con uno de los bloques disponibles del sistema", exception.getMessage());
    }
}
