package com.example.hack1base.events.domain;

import com.example.hack1base.salesaggregation.domain.SalesAggregates;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GithubModelsClient {

    @Value("${GITHUB_MODELS_URL}")
    private String modelsUrl;

    @Value("${MODEL_ID}")
    private String modelId;

    @Value("${GITHUB_TOKEN}")
    private String githubToken;


    private final WebClient webClient = WebClient.create();

    public String generateSummary(SalesAggregates aggregates) {
        String prompt = String.format(
                "Con estos datos: totalUnits=%d, totalRevenue=%.2f, topSku=%s, topBranch=%s. Devuelve un resumen ≤120 palabras en español para enviar por email.",
                aggregates.getTotalUnits(), aggregates.getTotalRevenue(), aggregates.getTopSku(), aggregates.getTopBranch()
        );

        Map<String, Object> body = Map.of(
                "model", modelId,
                "messages", List.of(
                        Map.of("role", "system", "content", "Eres un analista que escribe resúmenes breves y claros para emails corporativos."),
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 200
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(modelsUrl)
                    .header("Authorization", "Bearer " + githubToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            return ((Map<String, String>) ((List<?>) response.get("choices")).get(0))
                    .get("message").toString();

        } catch (Exception e) {
            throw new RuntimeException("Error consultando GitHub Models: " + e.getMessage());
        }
    }
}
