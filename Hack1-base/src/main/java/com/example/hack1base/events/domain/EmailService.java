package com.example.hack1base.events.domain;

import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${closetsale.mail.from:no-reply@oreo.com}")
    private String from;

    public void sendSummaryEmail(String to, LocalDate fromDate, LocalDate toDate,
                                 SalesAggregates aggregates, String summaryText) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Reporte Semanal Oreo - " + fromDate + " a " + toDate);
            helper.setFrom(from);

            String html = """
                <html>
                <body style="font-family:Arial,sans-serif;">
                    <h2>🍪 Reporte Semanal Oreo</h2>
                    <p>%s</p>
                    <h4>Resumen de ventas:</h4>
                    <ul>
                        <li><b>Total unidades:</b> %d</li>
                        <li><b>Ingresos totales:</b> $%.2f</li>
                        <li><b>SKU más vendido:</b> %s</li>
                        <li><b>Sucursal top:</b> %s</li>
                    </ul>
                </body>
                </html>
            """.formatted(summaryText, aggregates.getTotalUnits(), aggregates.getTotalRevenue(),
                    aggregates.getTopSku(), aggregates.getTopBranch());

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando email: " + e.getMessage());
        }
    }
}

