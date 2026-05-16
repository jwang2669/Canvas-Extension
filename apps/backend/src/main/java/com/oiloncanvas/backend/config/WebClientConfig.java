package com.oiloncanvas.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient beans for outbound HTTP (OpenAI, Canvas).
 * OpenAI: openai.api.key (e.g. OPENAI_API_KEY env var).
 * Canvas: canvas.base.url + canvas.access.token; bean only created when both are set.
 */
@Configuration
public class WebClientConfig {

  @Value("${openai.api.key}")
  private String apiKey;

  /**
   * Builds the WebClient used for OpenAI requests.
   *
   * Includes base URL plus default Authorization and JSON headers.
   *
   * @return configured WebClient bean for OpenAI API calls
   */
  @Bean
  public WebClient openAiWebClient() {
    return WebClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Bean("canvasWebClient")
  @ConditionalOnExpression("!'${canvas.base.url:}'.isBlank() && !'${canvas.access.token:}'.isBlank()")
  public WebClient canvasWebClient(
      @Value("${canvas.base.url}") String baseUrl,
      @Value("${canvas.access.token}") String accessToken) {
    String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    return WebClient.builder()
        .baseUrl(normalizedBase)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
