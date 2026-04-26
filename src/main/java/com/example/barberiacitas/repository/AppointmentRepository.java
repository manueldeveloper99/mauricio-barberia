package com.example.barberiacitas.repository;

import com.example.barberiacitas.model.Appointment;
import com.example.barberiacitas.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClienteEmail(String clienteEmail);
    List<Appointment> findByEstado(AppointmentStatus estado);
}
