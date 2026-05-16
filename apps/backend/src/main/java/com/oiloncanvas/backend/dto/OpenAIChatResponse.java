package com.oiloncanvas.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response body from OpenAI Chat Completions API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIChatResponse {

  /** Candidate completions returned by the model. */
  private List<Choice> choices;

  public List<Choice> getChoices() {
    return choices;
  }

  public void setChoices(List<Choice> choices) {
    this.choices = choices;
  }

  public static class Choice {
    /** Message payload for this choice. */
    private Message message;

    public Message getMessage() {
      return message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }
  }

  public static class Message {
    /** Role of message author (assistant/user/system). */
    private String role;
    /** Generated text for this message. */
    private String content;

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }
}
