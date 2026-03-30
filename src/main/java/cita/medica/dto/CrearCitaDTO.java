package cita.medica.dto;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**Crear/Actualizar */
public class CrearCitaDTO {


    @NotBlank( message = "El nombre del medico es obligatorio")
    private String nombreMedico;

    @NotNull(message = "La hora medica es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hora;

    @NotNull(message = "La fecha medica es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotBlank( message = "La especialidad es obligatoria")
    private String especialidad;
}
