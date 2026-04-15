package cita.medica.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cita.medica.Entity.CitaEntity;

public interface CitaRepository extends JpaRepository<CitaEntity, Long> {

    List<CitaEntity> findAllByOrderByIdAsc();

    List<CitaEntity> findByFechaAndNombreMedicoAndEspecialidadAndEstadoNot(
            LocalDate fecha,
            String nombreMedico,
            String especialidad,
            String estado);

    boolean existsByNombreMedicoAndFechaAndHoraAndEstadoNot(
            String nombreMedico,
            LocalDate fecha,
            LocalTime hora,
            String estado);
}
