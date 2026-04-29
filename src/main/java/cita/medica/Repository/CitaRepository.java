package cita.medica.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cita.medica.entity.CitaEntity;

@Repository
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
