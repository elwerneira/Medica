package cita.medica.Entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CITAS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cita_seq")
    @SequenceGenerator(name = "cita_seq", sequenceName = "CITA_SEQ", allocationSize = 1)
    private Long id;

    private String nombreMedico;
    private LocalDate fecha;
    private LocalTime hora;
    private String especialidad;
    private String estado;
}
