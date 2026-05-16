/**
 * NotificationBadge.jsx
 *
 * Placeholder UI that is not connected to backend
 */

import React from 'react';
import { CloseButton, Text } from '@instructure/ui';

export default function NotificationBadge({
  isOpen = true,
  onClose,
  title = 'ASSIGNMENT NAME',
  course = 'COURSE101: Course Name (001)',
  dueText = 'Feb 25 at 11pm',
  alertText = 'ALERT: Assignment Due in 2 hours.',
  onTitleClick,
}) {
  if (!isOpen) return null;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        {/* Header */}
        <div>
          <div style={styles.title}>
            <Text weight="bold" color="danger">
              {alertText}
            </Text>
          </div>
        </div>

        <div style={styles.closeButtonWrapper}>
          <CloseButton
            placement="end"
            offset="none"
            screenReaderLabel="Close notification"
            onClick={onClose}
          />
        </div>
      </div>

      {/* Body */}
      <div style={styles.content}>
        <button type="button" onClick={onTitleClick} style={styles.titleButton}>
          <Text>{title}</Text>
        </button>

        <div style={styles.courseText}>
          <Text weight="bold">{course}</Text>
        </div>

        <div style={styles.dueText}>
          <Text>{dueText}</Text>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    width: '350px',
    background: '#ffffff',
    border: '1px solid #d8dde6',
    borderRadius: '16px',
    boxShadow: '0 5px 20px rgba(0, 0, 0, 0.2)',
    overflow: 'hidden',
    fontFamily: 'Lato, "Helvetica Neue", Helvetica, Arial, sans-serif',
    display: 'flex',
    flexDirection: 'column',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    padding: '12px 14px',
    borderBottom: '1px solid #e6e9ec',
    background: '#f8f9fa',
  },
  alertText: {
    color: 'red',
    fontSize: '14px',
    lineHeight: 1.2,
    flex: 1,
  },
  closeButtonWrapper: {
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'center',
    position: 'relative',
  },
  content: {
    padding: '14px',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    background: '#fcfcfd',
  },
  titleButton: {
    background: 'none',
    border: 'none',
    padding: 0,
    margin: 0,
    textAlign: 'left',
    cursor: 'pointer',
    color: '#0f68b2',
    fontSize: '14px',
    textDecoration: 'underline',
    fontFamily: 'inherit',
  },
  courseText: {
    color: '#586973',
    fontSize: '13px',
  },
  dueText: {
    color: '#273540',
    fontSize: '13px',
  },
};
