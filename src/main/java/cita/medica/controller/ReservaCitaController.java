package cita.medica.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cita.medica.dto.CrearCitaDTO;
import cita.medica.dto.ReservaCitaDTO;
import cita.medica.service.ReservaCitaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Validated
@RequestMapping("/reservas")
public class ReservaCitaController {

    private final ReservaCitaService service;

    public ReservaCitaController(ReservaCitaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ReservaCitaDTO>>> obtenerTodas() {
        List<EntityModel<ReservaCitaDTO>> reservas = service.obtenerTodas().stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<ReservaCitaDTO>> response = CollectionModel.of(
                reservas,
                linkTo(methodOn(ReservaCitaController.class).obtenerTodas()).withSelfRel(),
                linkTo(methodOn(ReservaCitaController.class).consultaDisponibilidad(null, null, null))
                        .withRel("disponibilidad"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        ReservaCitaDTO reserva = service.obtenerPorId(id);

        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita Medica no encontrada: " + id);
        }

        return ResponseEntity.ok(toModel(reserva));
    }

    @PostMapping
    public ResponseEntity<EntityModel<ReservaCitaDTO>> crear(@Valid @RequestBody CrearCitaDTO request) {
        ReservaCitaDTO creada = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody CrearCitaDTO request) {
        ReservaCitaDTO actualizada = service.actualizar(id, request);

        if (actualizada == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita Medica no encontrada, ID: " + id);
        }

        return ResponseEntity.ok(toModel(actualizada));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        ReservaCitaDTO cancelada = service.cancelar(id);

        if (cancelada == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita Medica no encontrada, ID: " + id);
        }

        return ResponseEntity.ok(toModel(cancelada));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<EntityModel<Map<String, Object>>> consultaDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @NotBlank(message = "El nombre del medico es obligatorio") String nombreMedico,
            @RequestParam @NotBlank(message = "La especialidad es obligatoria") String especialidad) {

        Map<String, Object> disponibilidad = service.consultarDisponibilidad(fecha, nombreMedico, especialidad);
        EntityModel<Map<String, Object>> response = EntityModel.of(
                disponibilidad,
                linkTo(methodOn(ReservaCitaController.class)
                        .consultaDisponibilidad(fecha, nombreMedico, especialidad)).withSelfRel(),
                linkTo(methodOn(ReservaCitaController.class).obtenerTodas()).withRel("reservas"));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean eliminada = service.eliminar(id);

        if (!eliminada) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita no encontrada. ID: " + id);
        }

        return ResponseEntity.noContent().build();
    }

    private EntityModel<ReservaCitaDTO> toModel(ReservaCitaDTO reserva) {
        return EntityModel.of(
                reserva,
                linkTo(methodOn(ReservaCitaController.class).obtenerPorId(reserva.getId())).withSelfRel(),
                linkTo(methodOn(ReservaCitaController.class).obtenerTodas()).withRel("reservas"),
                linkTo(methodOn(ReservaCitaController.class).actualizar(reserva.getId(), null)).withRel("actualizar"),
                linkTo(methodOn(ReservaCitaController.class).cancelar(reserva.getId())).withRel("cancelar"),
                linkTo(methodOn(ReservaCitaController.class).eliminar(reserva.getId())).withRel("eliminar"));
    }
}
