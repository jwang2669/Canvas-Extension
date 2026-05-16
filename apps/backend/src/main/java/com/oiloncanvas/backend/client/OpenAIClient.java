package com.oiloncanvas.backend.client;

import com.oiloncanvas.backend.dto.OpenAIChatRequest;
import com.oiloncanvas.backend.dto.OpenAIChatResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Outbound HTTP client for OpenAI Chat Completions API.
 * Used by {@link com.oiloncanvas.backend.service.SuggestionService}.
 */
@Component
public class OpenAIClient {

  private final WebClient webClient;
  private final String model;

  public OpenAIClient(
      @Qualifier("openAiWebClient") WebClient webClient,
      @Value("${openai.api.model:gpt-4o-mini}") String model) {
    this.webClient = webClient;
    this.model = model;
  }

  /**
   * Sends the given prompt to OpenAI and returns the assistant reply text.
   *
   * @param prompt user message content
   * @return assistant content, or a fallback message if response is empty or request fails
   */
  public String getSuggestion(String prompt) {
    return getChatCompletion(List.of(new OpenAIChatRequest.Message("user", prompt)));
  }

  /**
   * Sends the given message list (e.g. system + user) to OpenAI and returns the assistant reply.
   *
   * @param messages ordered roles and content; must not be null or empty
   */
  public String getChatCompletion(List<OpenAIChatRequest.Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return "No suggestion generated.";
    }
    try {
      OpenAIChatRequest body = new OpenAIChatRequest(model, messages);

      OpenAIChatResponse response =
          webClient
              .post()
              .uri("/chat/completions")
              .bodyValue(body)
              .retrieve()
              .bodyToMono(OpenAIChatResponse.class)
              .block();

      if (response == null
          || response.getChoices() == null
          || response.getChoices().isEmpty()) {
        return "No suggestion generated.";
      }

      OpenAIChatResponse.Message message = response.getChoices().get(0).getMessage();
      if (message == null || message.getContent() == null || message.getContent().isBlank()) {
        return "No suggestion generated.";
      }

      return message.getContent().trim();
    } catch (Exception e) {
      return "Suggestion unavailable. Check OPENAI_API_KEY and network.";
    }
  }
}
