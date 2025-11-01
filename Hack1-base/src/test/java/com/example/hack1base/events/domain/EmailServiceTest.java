package com.example.hack1base.events.domain;


import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import com.example.hack1base.Exceptions.ServiceUnavailableException;
import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("shouldSendHtmlEmailWhenInputsAreValid")
    void shouldSendHtmlEmailWhenInputsAreValid() throws Exception {
        MimeMessage msg = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(msg);

        ReflectionTestUtils.setField(emailService, "from", "no-reply@oreo.com");

        String to = "dest@corp.com";
        LocalDate fromDate = LocalDate.of(2025, 1, 1);
        LocalDate toDate   = LocalDate.of(2025, 1, 7);

        SalesAggregates aggregates = new SalesAggregates(
                42,
                1234.56,
                "SKU-ABC",
                "Miraflores"
        );
        String summary = "Resumen semanal generado automáticamente.";


        emailService.sendSummaryEmail(to, fromDate, toDate, aggregates, summary);


        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        MimeMessage sent = captor.getValue();


        assertEquals("Reporte Semanal Oreo - 2025-01-01 a 2025-01-07", sent.getSubject());

        Address[] recipients = sent.getAllRecipients();
        assertNotNull(recipients);
        assertEquals(1, recipients.length);
        assertEquals("dest@corp.com", ((InternetAddress) recipients[0]).getAddress());

        Address[] froms = sent.getFrom();
        assertNotNull(froms);
        assertEquals(1, froms.length);
        assertEquals("no-reply@oreo.com", ((InternetAddress) froms[0]).getAddress());


        Object content = sent.getContent();
        assertInstanceOf(String.class, content);
        String html = (String) content;

        assertTrue(html.contains("🍪 Reporte Semanal Oreo"));
        assertTrue(html.contains(summary));
        assertTrue(html.contains("<li><b>Total unidades:</b> 42</li>"));
        assertTrue(html.contains("<li><b>Ingresos totales:</b> $1234.56</li>"));
        assertTrue(html.contains("<li><b>SKU más vendido:</b> SKU-ABC</li>"));
        assertTrue(html.contains("<li><b>Sucursal top:</b> Miraflores</li>"));
    }

    @Test
    @DisplayName("shouldThrowServiceUnavailableWhenMailSenderFails")
    void shouldThrowServiceUnavailableWhenMailSenderFails() throws Exception {

        MimeMessage msg = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(msg);
        ReflectionTestUtils.setField(emailService, "from", "no-reply@oreo.com");


        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        SalesAggregates aggregates = new SalesAggregates(1, 10.0, "SKU-X", "Surco");


        ServiceUnavailableException ex = assertThrows(
                ServiceUnavailableException.class,
                () -> emailService.sendSummaryEmail("x@y.com",
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 1, 7),
                        aggregates,
                        "texto")
        );
        assertTrue(ex.getMessage().contains("Error enviando email"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}

