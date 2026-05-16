package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Request body for OpenAI Chat Completions API (POST /chat/completions).
 */
public class OpenAIChatRequest {

  /** OpenAI model id (for example, gpt-4o-mini). */
  private String model;
  /** Ordered chat messages sent to the model. */
  private List<Message> messages;

  public OpenAIChatRequest() {}

  public OpenAIChatRequest(String model, List<Message> messages) {
    this.model = model;
    this.messages = messages;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }

  public static class Message {
    /** Role of message author (user/system/assistant). */
    private String role;
    /** Message text content. */
    private String content;

    public Message() {}

    public Message(String role, String content) {
      this.role = role;
      this.content = content;
    }

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
