package com.example.hack1base.ReportRequest.domain;

import com.example.hack1base.ReportRequest.estructrure.ReportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportRequestService {

    private final ReportRequestRepository reportRequestRepository;

    // Crear un nuevo reporte
    public ReportRequest createReportRequest(ReportRequest reportRequest) {
        return reportRequestRepository.save(reportRequest);
    }

    // Obtener todos los reportes
    public List<ReportRequest> getAllReportRequests() {
        return reportRequestRepository.findAll();
    }

    // Buscar reporte por ID
    public Optional<ReportRequest> getReportRequestById(Long id) {
        return reportRequestRepository.findById(id);
    }

    // Actualizar el estado o mensaje de un reporte
    public ReportRequest updateReportRequest(Long id, ReportRequest updatedRequest) {
        return reportRequestRepository.findById(id)
                .map(report -> {
                    report.setBranch(updatedRequest.getBranch());
                    report.setFromDate(updatedRequest.getFromDate());
                    report.setToDate(updatedRequest.getToDate());
                    report.setEmailTo(updatedRequest.getEmailTo());
                    report.setStatus(updatedRequest.getStatus());
                    report.setMessage(updatedRequest.getMessage());
                    return reportRequestRepository.save(report);
                })
                .orElseThrow(() -> new RuntimeException("ReportRequest not found"));
    }

    // Eliminar un reporte
    public void deleteReportRequest(Long id) {
        reportRequestRepository.deleteById(id);
    }
}
