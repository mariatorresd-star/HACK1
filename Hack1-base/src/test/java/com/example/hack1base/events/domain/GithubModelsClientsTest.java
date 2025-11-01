package com.example.hack1base.events.domain;


import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GithubModelsClientsTest {

    private MockWebServer server;

    private GithubModelsClients newClientPointingTo(String url, String modelId, String token) throws Exception {
        GithubModelsClients client = new GithubModelsClients();
        ReflectionTestUtils.setField(client, "modelsUrl", url);
        ReflectionTestUtils.setField(client, "modelId", modelId);
        ReflectionTestUtils.setField(client, "githubToken", token);
        WebClient wc = WebClient.builder().build();
        ReflectionTestUtils.setField(client, "webClient", wc);
        return client;
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    private SalesAggregates sampleAgg() {
        return new SalesAggregates(25, 999.5, "SKU-1", "Miraflores");
    }

    @Test
    @DisplayName("shouldReturnContentWhenResponseStructureIsValid")
    void shouldReturnContentWhenResponseStructureIsValid() throws Exception {
        // Arrange
        server = new MockWebServer();
        server.start();
        String endpoint = server.url("/v1/chat/completions").toString();

        // Respuesta válida simulada por GitHub Models
        String bodyJson = """
            {
              "choices": [
                {
                  "message": { "content": "Resumen generado por el modelo." }
                }
              ]
            }
            """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bodyJson)
                .addHeader("Content-Type", "application/json"));

        GithubModelsClients client =
                newClientPointingTo(endpoint, "gpt-test", "ghp_123456");

        // Act
        String out = client.generateSummary(sampleAgg());

        // Assert
        assertEquals("Resumen generado por el modelo.", out);

        // Verificamos la request enviada
        var recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/v1/chat/completions", recorded.getPath());
        assertEquals("Bearer ghp_123456", recorded.getHeader("Authorization"));
        assertEquals("application/json", recorded.getHeader("Content-Type"));

        String sentBody = recorded.getBody().readString(StandardCharsets.UTF_8);
        assertTrue(sentBody.contains("\"model\":\"gpt-test\""));
        assertTrue(sentBody.contains("\"role\":\"system\""));
        assertTrue(sentBody.contains("\"role\":\"user\""));
        assertTrue(sentBody.contains("totalUnits=25"));
        assertTrue(sentBody.contains("totalRevenue=999.50"));
        assertTrue(sentBody.contains("topSku=SKU-1"));
        assertTrue(sentBody.contains("topBranch=Miraflores"));
    }

    @Test
    @DisplayName("shouldThrowRuntimeWhenChoicesIsEmpty")
    void shouldThrowRuntimeWhenChoicesIsEmpty() throws Exception {
        // Arrange
        server = new MockWebServer();
        server.start();
        String endpoint = server.url("/v1/chat/completions").toString();

        // Respuesta con choices vacío
        String bodyJson = """
            { "choices": [] }
            """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bodyJson)
                .addHeader("Content-Type", "application/json"));

        GithubModelsClients client =
                newClientPointingTo(endpoint, "gpt-test", "token");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.generateSummary(sampleAgg()));
        assertTrue(ex.getMessage().contains("choices vacío"));
    }

    @Test
    @DisplayName("shouldThrowRuntimeWhenResponseHasMissingFields")
    void shouldThrowRuntimeWhenResponseHasMissingFields() throws Exception {
        // Arrange
        server = new MockWebServer();
        server.start();
        String endpoint = server.url("/v1/chat/completions").toString();

        // Respuesta sin 'choices'
        String bodyJson = """
            { "unexpected": "structure" }
            """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(bodyJson)
                .addHeader("Content-Type", "application/json"));

        GithubModelsClients client =
                newClientPointingTo(endpoint, "gpt-test", "token");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.generateSummary(sampleAgg()));
        assertTrue(ex.getMessage().contains("choices vacío"));
    }

    @Test
    @DisplayName("shouldWrapNetworkErrorsIntoRuntimeException")
    void shouldWrapNetworkErrorsIntoRuntimeException() throws Exception {

        server = new MockWebServer();
        server.start();
        String endpoint = server.url("/v1/chat/completions").toString();

        server.shutdown();

        GithubModelsClients client =
                newClientPointingTo(endpoint, "gpt-test", "token");


        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> client.generateSummary(sampleAgg()));
        assertTrue(ex.getMessage().startsWith("Error consultando GitHub Models"));
    }
}
