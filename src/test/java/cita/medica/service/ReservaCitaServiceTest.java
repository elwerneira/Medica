package cita.medica.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cita.medica.dto.CrearCitaDTO;
import cita.medica.dto.ReservaCitaDTO;
import cita.medica.entity.CitaEntity;
import cita.medica.repository.CitaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaCitaServiceTest {

    @Mock
    private CitaRepository repository;

    @InjectMocks
    private ReservaCitaService service;

    private CrearCitaDTO request;
    private CitaEntity reservaGuardada;

    @BeforeEach
    void setUp() {
        request = CrearCitaDTO.builder()
                .nombreMedico("Dra. Ana Perez")
                .fecha(LocalDate.of(2026, 5, 10))
                .hora(LocalTime.of(9, 0))
                .especialidad("Cardiologia")
                .build();

        reservaGuardada = CitaEntity.builder()
                .id(1L)
                .nombreMedico(request.getNombreMedico())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .especialidad(request.getEspecialidad())
                .estado("Reservada")
                .build();
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(repository);
    }

    @Test
    void crearDebeGuardarReservaConEstadoReservada() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of());
        when(repository.save(any(CitaEntity.class))).thenReturn(reservaGuardada);

        ReservaCitaDTO resultado = service.crear(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Dra. Ana Perez", resultado.getNombreMedico());
        assertEquals("Cardiologia", resultado.getEspecialidad());
        assertEquals("Reservada", resultado.getEstado());
        verify(repository).findAllByOrderByIdAsc();
        verify(repository).save(any(CitaEntity.class));
    }

    @Test
    void cancelarDebeCambiarEstadoAAnulada() {
        CitaEntity reserva = CitaEntity.builder()
                .id(1L)
                .nombreMedico("Dra. Ana Perez")
                .fecha(LocalDate.of(2026, 5, 10))
                .hora(LocalTime.of(9, 0))
                .especialidad("Cardiologia")
                .estado("Reservada")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        when(repository.save(reserva)).thenReturn(reserva);

        ReservaCitaDTO resultado = service.cancelar(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Anulada", resultado.getEstado());
        verify(repository).findById(1L);
        verify(repository).save(reserva);
    }

    @Test
    void crearDebeRechazarHorarioNoPermitido() {
        request.setHora(LocalTime.of(13, 0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.crear(request));

        assertEquals("La hora debe coincidir con uno de los bloques disponibles del sistema", exception.getMessage());
    }

    @Test
    void eliminarDebeRetornarFalseCuandoReservaNoExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        boolean resultado = service.eliminar(99L);

        assertFalse(resultado);
        verify(repository).existsById(99L);
        verify(repository, never()).deleteById(99L);
    }
}
