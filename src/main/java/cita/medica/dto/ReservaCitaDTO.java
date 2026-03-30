package cita.medica.dto;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**Reserva */
public class ReservaCitaDTO {
    
    private Long id;
    private String nombreMedico;
    private LocalDate fecha;
    private LocalTime hora;
    private String especialidad;
    private String estado;
}
