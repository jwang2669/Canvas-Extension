/**
 * Chatroom.jsx
 *
 * A fixed chat panel that appears on the right side of the Canvas page:
 * - header at the top
 * - scrollable messages area in the middle
 * - input row at the bottom
 *
 * Styling is mostly custom CSS-in-JS to preserve the exact layout,
 * while text and buttons use Instructure UI so they inherit the Canvas theme.
 */

import React, { useEffect, useRef, useState } from 'react';
import { Button, CloseButton, Text } from '@instructure/ui';
import { BACKEND_BASE_URL } from '../config.js';

function buildAssistantGreeting(userDisplayName) {
  const name = typeof userDisplayName === 'string' ? userDisplayName.trim() : '';
  if (name.length > 0) {
    return `Hi, ${name}! This is your AI Canvas Assistant. How can I help you today?`;
  }
  return 'Hi! This is your AI Canvas Assistant. How can I help you today?';
}

export default function Chatroom({
  isOpen = true,
  onClose,
  sessionId = null,
  userDisplayName = null,
}) {
  const [messages, setMessages] = useState([]);

  // Tracks the current contents of the user's input box
  const [input, setInput] = useState('');

  // Prevents duplicate sends and lets us show a "Thinking..." state
  const [isSending, setIsSending] = useState(false);

  const [status, setStatus] = useState('loading...');
  const [error, setError] = useState(null);

  // Reference to an invisible element at the bottom of the chat that we scroll to when a new message is added
  const messagesEndRef = useRef(null);
  // Reference to the textarea to return focus after sending.
  const textareaRef = useRef(null);

  useEffect(() => {
    fetch(`${BACKEND_BASE_URL}/api/health`)
      .then((res) => res.json())
      .then((data) => setStatus(data.status))
      .catch((err) => setError(err.message));
  }, []);

  // Opening the panel (or learning the user's name) sets a personalized greeting; keep thread if the user already chatted.
  useEffect(() => {
    if (!isOpen) return;
    setMessages((prev) => {
      if (prev.some((m) => m.role === 'user')) {
        return prev;
      }
      return [
        {
          id: Date.now(),
          role: 'assistant',
          text: buildAssistantGreeting(userDisplayName),
        },
      ];
    });
  }, [isOpen, userDisplayName]);

  // Whenever messages change or the chat opens, keep the newest message visible
  useEffect(() => {
    scrollToBottom();
  }, [messages, isOpen]);

  // Scrolls to the bottom of the chat history
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // Sends the user's message, calls the backend handler, and appends the assistant's response to the chat
  const handleSend = async () => {
    const trimmed = input.trim();

    // Do nothing for empty messages or while a send is already in progress
    if (!trimmed || isSending) return;

    // Add the user's message immediately
    const userMessage = {
      id: Date.now(),
      role: 'user',
      text: trimmed,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsSending(true);

    try {
      const response = await fetch(`${BACKEND_BASE_URL}/api/suggestions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          prompt: trimmed,
          ...(sessionId ? { sessionId } : {}),
          ...(userDisplayName && String(userDisplayName).trim()
            ? { userDisplayName: String(userDisplayName).trim() }
            : {}),
        }),
      });

      if (!response.ok) {
        throw new Error(`Backend request failed with status ${response.status}`);
      }

      const data = await response.json();

      // Backend JSON: { recommendation: "AI response here" }
      const replyText = data?.recommendation?.trim() || 'No suggestion generated.';

      // Append the assistant reply after the async call completes
      const botMessage = {
        id: Date.now() + 1, // Add 1 to ID to differentiate from the user's message ID
        role: 'assistant',
        text: replyText,
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (err) {
      const errorMessage = {
        id: Date.now() + 2,
        role: 'assistant',
        text: `Sorry, something went wrong: ${err.message}`,
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsSending(false);
      textareaRef.current?.focus();
    }
  };

  // Press Enter to send or Shift+Enter for a newline
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // Do not render anything when the chat panel is closed
  if (!isOpen) return null;

  return (
    <div style={styles.container}>
      {/* Header with title and close button */}
      <div style={styles.header}>
        <div>
          <div style={styles.title}>
            <Text weight="bold">Chat</Text>
          </div>
        </div>

        {/* Instructure close button */}
        <div style={styles.closeButtonWrapper}>
          <CloseButton
            placement="end"
            offset="none"
            screenReaderLabel="Close chatroom"
            onClick={onClose}
          />
        </div>
      </div>

      {/* Main scrollable chat area */}
      <div style={styles.messagesArea}>
        {messages.map((msg) => (
          <div
            key={msg.id}
            style={{
              ...styles.messageRow,

              // User messages align right and assistant messages align left
              justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
            }}
          >
            <div
              style={{
                ...styles.messageBubble,
                // Apply different bubble styling for user vs assistant
                ...(msg.role === 'user' ? styles.userBubble : styles.assistantBubble),
              }}
            >
              {/* Use InstUI Text */}
              <Text size="small" color="primary">
                {msg.text}
              </Text>
            </div>
          </div>
        ))}

        {/* Temporary loading bubble shown while waiting for assistant reply */}
        {isSending && (
          <div style={styles.messageRow}>
            <div style={{ ...styles.messageBubble, ...styles.assistantBubble }}>
              <Text color="secondary">Thinking...</Text>
            </div>
          </div>
        )}

        {/* Invisible anchor for auto-scrolling */}
        <div ref={messagesEndRef} />
      </div>

      {/* Bottom input area: textbox on the left, send button on the right */}
      <div style={styles.inputArea}>
        <textarea
          ref={textareaRef}
          style={styles.textarea}
          placeholder="Ask a question"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          rows={2}
        />

        {/* Wrapper keeps the button aligned with the textbox */}
        <div style={styles.sendButtonWrapper}>
          <Button color="primary" onClick={handleSend} disabled={!input.trim() || isSending}>
            Send
          </Button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  // Fixed chat panel on the right side and stays visible while scrolling
  container: {
    position: 'fixed',
    top: '100px',
    right: '24px',
    width: '300px',
    height: '75vh',
    minHeight: '500px',
    maxHeight: '800px',
    background: '#ffffff',
    border: '1px solid #d8dde6',
    borderRadius: '16px',
    boxShadow: '0 5px 20px rgba(0, 0, 0, 0.2)',
    zIndex: 2147483647,
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
    fontFamily: 'Lato, "Helvetica Neue", Helvetica, Arial, sans-serif',
  },

  // Top bar containing title and the close button
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    padding: '14px 16px',
    borderBottom: '1px solid #e6e9ec',
    background: '#f8f9fa',
  },

  // Wrapper for the title text
  title: {
    fontSize: '16px',
    color: '#1f2937',
  },

  // Keeps the close button vertically centered in the header
  closeButtonWrapper: {
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'center',
    position: 'relative',
  },

  // Scrollable region where all messages are shown
  messagesArea: {
    flex: 1,
    overflowY: 'auto',
    padding: '14px',
    background: '#fcfcfd',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },

  // Each message gets its own full-width row so it can align left or right
  messageRow: {
    display: 'flex',
    width: '100%',
  },

  // Shared styling for all chat bubbles
  messageBubble: {
    maxWidth: '85%',
    padding: '10px 12px',
    borderRadius: '14px',
    fontSize: '10px',
    lineHeight: 1.2,
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-word',
    fontFamily: 'inherit',
  },

  // Slightly darker grey for user messages
  userBubble: {
    background: '#e5e7eb',
    color: '#1f2937',
    border: '1px solid #d1d5db',
    borderBottomRightRadius: '2px',
  },

  // Slightly lighter grey for assistant messages
  assistantBubble: {
    background: '#f3f4f6',
    color: '#1f2937',
    border: '1px solid #d8dde6',
    borderBottomLeftRadius: '2px',
  },

  // Bottom area with the text area and the send button
  inputArea: {
    borderTop: '1px solid #e6e9ec',
    background: '#ffffff',
    padding: '12px',
    display: 'flex',
    gap: '10px',
    alignItems: 'flex-start',
  },

  // Text input styling
  textarea: {
    width: '100%',
    resize: 'none',
    border: '1px solid #c7cdd4',
    borderRadius: '12px',
    padding: '10px 12px',
    fontSize: '14px',
    lineHeight: 1.2,
    height: '38px',
    minHeight: '38px',
    maxHeight: '44px',
    fontFamily: 'inherit',
    boxSizing: 'border-box',
    overflow: 'hidden',
  },

  // Button wrapper used to keep the Send button aligned with the textbox
  sendButtonWrapper: {
    height: '44px',
    display: 'flex',
    alignItems: 'flex-start',
  },
};
