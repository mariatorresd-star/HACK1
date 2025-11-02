package com.example.hack1base.SalesAggregation.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import static org.junit.jupiter.api.Assertions.*;

class SalesAggregatesTest {

    @Test
    @DisplayName("AllArgsConstructor: asigna correctamente todos los campos")
    void shouldCreateWithAllArgsConstructor() {

        int totalUnits = 10;
        double totalRevenue = 1234.56;
        String topSku = "SKU-001";
        String topBranch = "Lima-Centro";


        SalesAggregates aggregates = new SalesAggregates(totalUnits, totalRevenue, topSku, topBranch);


        assertAll(
                () -> assertEquals(totalUnits, aggregates.getTotalUnits()),
                () -> assertEquals(totalRevenue, aggregates.getTotalRevenue()),
                () -> assertEquals(topSku, aggregates.getTopSku()),
                () -> assertEquals(topBranch, aggregates.getTopBranch())
        );
    }

    @Test
    @DisplayName("NoArgsConstructor + setters: permite mutar valores correctamente")
    void shouldAllowMutationWithSetters() {

        SalesAggregates aggregates = new SalesAggregates();


        aggregates.setTotalUnits(25);
        aggregates.setTotalRevenue(999.99);
        aggregates.setTopSku("SKU-XYZ");
        aggregates.setTopBranch("Surco");


        assertAll(
                () -> assertEquals(25, aggregates.getTotalUnits()),
                () -> assertEquals(999.99, aggregates.getTotalRevenue()),
                () -> assertEquals("SKU-XYZ", aggregates.getTopSku()),
                () -> assertEquals("Surco", aggregates.getTopBranch())
        );
    }

    @Test
    @DisplayName("NoArgsConstructor: inicializa con valores por defecto (0 o null)")
    void shouldInitializeDefaultsWithNoArgsConstructor() {

        SalesAggregates aggregates = new SalesAggregates();


        assertAll(
                () -> assertEquals(0, aggregates.getTotalUnits()),
                () -> assertEquals(0.0, aggregates.getTotalRevenue()),
                () -> assertNull(aggregates.getTopSku()),
                () -> assertNull(aggregates.getTopBranch())
        );
    }

    @Test
    @DisplayName("equals/hashCode: dos instancias con mismos valores son iguales")
    void shouldRespectEqualsAndHashCode() {

        SalesAggregates a = new SalesAggregates(5, 500.0, "SKU-5", "Miraflores");
        SalesAggregates b = new SalesAggregates(5, 500.0, "SKU-5", "Miraflores");


        boolean equalsAB = a.equals(b);
        int hashA = a.hashCode();
        int hashB = b.hashCode();


        assertAll(
                () -> assertTrue(equalsAB, "Deberían ser iguales por valor"),
                () -> assertEquals(hashA, hashB, "hashCode debe coincidir para objetos iguales")
        );
    }

    @Test
    @DisplayName("equals: instancias con valores distintos NO son iguales")
    void shouldNotBeEqualWhenFieldsDiffer() {

        SalesAggregates a = new SalesAggregates(5, 500.0, "SKU-5", "Miraflores");
        SalesAggregates c = new SalesAggregates(6, 500.0, "SKU-5", "Miraflores");

        boolean equalsAC = a.equals(c);

        assertFalse(equalsAC, "No deben ser iguales si al menos un campo difiere");
    }

    @Test
    @DisplayName("equals: cumple propiedades reflexiva y simétrica")
    void shouldBeReflexiveAndSymmetric() {
        SalesAggregates a = new SalesAggregates(5, 500.0, "SKU-5", "Miraflores");
        SalesAggregates b = new SalesAggregates(5, 500.0, "SKU-5", "Miraflores");

        assertAll(
                () -> assertTrue(a.equals(a), "Debe ser reflexivo"),
                () -> assertTrue(a.equals(b) && b.equals(a), "Debe ser simétrico")
        );
    }

    @Test
    @DisplayName("toString: incluye nombre de la clase y valores principales")
    void shouldProduceReadableToString() {
        // Arrange
        SalesAggregates aggregates = new SalesAggregates(10, 123.45, "SKU-10", "San Isidro");

        // Act
        String text = aggregates.toString();

        // Assert
        assertAll(
                () -> assertTrue(text.contains("SalesAggregates")),
                () -> assertTrue(text.contains("totalUnits=10")),
                () -> assertTrue(text.contains("totalRevenue=123.45")),
                () -> assertTrue(text.contains("topSku=SKU-10")),
                () -> assertTrue(text.contains("topBranch=San Isidro"))
        );
    }

    @Test
    @DisplayName("Permite valores límite (0 y null)")
    void shouldAcceptBoundaryValues() {
        SalesAggregates aggregates = new SalesAggregates(0, 0.0, null, null);

        assertAll(
                () -> assertEquals(0, aggregates.getTotalUnits()),
                () -> assertEquals(0.0, aggregates.getTotalRevenue()),
                () -> assertNull(aggregates.getTopSku()),
                () -> assertNull(aggregates.getTopBranch())
        );
    }
}
