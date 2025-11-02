package com.example.hack1base.events.dto;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SummaryRequestTest {

    @Test
    @DisplayName("shouldCreateEmptyObjectWhenUsingNoArgsConstructor")
    void shouldCreateEmptyObjectWhenUsingNoArgsConstructor() {

        SummaryRequest req = new SummaryRequest();


        assertAll(
                () -> assertNull(req.getFrom(), "El campo 'from' debería ser null"),
                () -> assertNull(req.getTo(), "El campo 'to' debería ser null"),
                () -> assertNull(req.getBranch(), "El campo 'branch' debería ser null"),
                () -> assertNull(req.getEmailTo(), "El campo 'emailTo' debería ser null")
        );
    }

    @Test
    @DisplayName("shouldAssignAllFieldsWhenUsingAllArgsConstructor")
    void shouldAssignAllFieldsWhenUsingAllArgsConstructor() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 7);
        String branch = "Miraflores";
        String emailTo = "report@oreo.com";


        SummaryRequest req = new SummaryRequest(from, to, branch, emailTo);


        assertAll(
                () -> assertEquals(from, req.getFrom()),
                () -> assertEquals(to, req.getTo()),
                () -> assertEquals(branch, req.getBranch()),
                () -> assertEquals(emailTo, req.getEmailTo())
        );
    }

    @Test
    @DisplayName("shouldAllowSettersAndGettersToModifyFields")
    void shouldAllowSettersAndGettersToModifyFields() {

        SummaryRequest req = new SummaryRequest();
        LocalDate from = LocalDate.of(2025, 2, 1);
        LocalDate to = LocalDate.of(2025, 2, 10);
        String branch = "Surco";
        String emailTo = "surco@oreo.com";


        req.setFrom(from);
        req.setTo(to);
        req.setBranch(branch);
        req.setEmailTo(emailTo);


        assertAll(
                () -> assertEquals(from, req.getFrom()),
                () -> assertEquals(to, req.getTo()),
                () -> assertEquals(branch, req.getBranch()),
                () -> assertEquals(emailTo, req.getEmailTo())
        );
    }

    @Test
    @DisplayName("shouldBeEqualWhenAllFieldsAreSame")
    void shouldBeEqualWhenAllFieldsAreSame() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 7);
        SummaryRequest r1 = new SummaryRequest(from, to, "Lima", "a@b.com");
        SummaryRequest r2 = new SummaryRequest(from, to, "Lima", "a@b.com");


        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("shouldNotBeEqualWhenFieldsDiffer")
    void shouldNotBeEqualWhenFieldsDiffer() {

        SummaryRequest r1 = new SummaryRequest(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 7), "Lima", "a@b.com");
        SummaryRequest r2 = new SummaryRequest(LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 8), "Surco", "b@b.com");

        // Act & Assert
        assertNotEquals(r1, r2);
    }

    @Test
    @DisplayName("shouldGenerateToStringContainingAllFields")
    void shouldGenerateToStringContainingAllFields() {

        SummaryRequest req = new SummaryRequest(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 7),
                "Miraflores",
                "oreo@corp.com"
        );


        String text = req.toString();


        assertAll(
                () -> assertTrue(text.contains("from=2025-03-01")),
                () -> assertTrue(text.contains("to=2025-03-07")),
                () -> assertTrue(text.contains("branch=Miraflores")),
                () -> assertTrue(text.contains("emailTo=oreo@corp.com"))
        );
    }
}
