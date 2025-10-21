package com.example.hack1base.ReportRequest.estructrure;


import com.example.hack1base.ReportRequest.domain.ReportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRequestRepository extends JpaRepository<ReportRequest, String> { }
