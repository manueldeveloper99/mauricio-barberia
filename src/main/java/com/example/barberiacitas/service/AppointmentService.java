package com.example.barberiacitas.service;

import com.example.barberiacitas.model.Appointment;
import com.example.barberiacitas.model.AppointmentStatus;
import com.example.barberiacitas.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> getAllAppointments(String clienteEmail, String clienteNombre) {
        return appointmentRepository.findAll().stream()
                .filter(a -> clienteEmail == null || clienteEmail.isEmpty() || a.getClienteEmail().toLowerCase().contains(clienteEmail.toLowerCase()))
                .filter(a -> clienteNombre == null || clienteNombre.isEmpty() || a.getClienteNombre().toLowerCase().contains(clienteNombre.toLowerCase()))
                .toList();
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        if (appointment.getFechaHora() == null || appointment.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha y hora debe ser en el futuro.");
        }

        if (appointment.getDuracionMin() == null) {
            appointment.setDuracionMin(30); // Default duration
        }

        LocalDateTime inicioB = appointment.getFechaHora();
        LocalDateTime finB = inicioB.plusMinutes(appointment.getDuracionMin());

        List<Appointment> reservadas = appointmentRepository.findByEstado(AppointmentStatus.RESERVADA);

        for (Appointment existente : reservadas) {
            LocalDateTime inicioA = existente.getFechaHora();
            LocalDateTime finA = inicioA.plusMinutes(existente.getDuracionMin());

            // Overlap condition: inicioA < finB && inicioB < finA
            if (inicioA.isBefore(finB) && inicioB.isBefore(finA)) {
                throw new IllegalStateException("¡Ups! El barbero ya está ocupado en ese horario. Por favor, elige otra hora.");
            }
        }

        appointment.setEstado(AppointmentStatus.RESERVADA);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public boolean cancelAppointment(Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setEstado(AppointmentStatus.CANCELADA);
            appointmentRepository.save(appointment);
            return true;
        }).orElse(false);
    }
}
