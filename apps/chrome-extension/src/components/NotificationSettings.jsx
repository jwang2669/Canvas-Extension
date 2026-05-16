import React, { useState, useEffect } from 'react';
import { Button, CloseButton, Text } from '@instructure/ui';

export default function NotificationSettings({
  isOpen = true,
  onClose,
  assignmentRemindersEnabled,
  onSaveSettings,
}) {
  const [localAssignmentRemindersEnabled, setLocalAssignmentRemindersEnabled] = useState(
    assignmentRemindersEnabled ?? true,
  );

  // How far in advance to send notifications to the user
  const [reminderDaysBefore, setReminderDaysBefore] = useState('2');
  const [reminderHoursBefore, setReminderHoursBefore] = useState('12');

  // Other notification settings
  const [overdueAlertsEnabled, setOverdueAlertsEnabled] = useState(true);
  const [urgentOnly, setUrgentOnly] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem('notificationSettings');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setLocalAssignmentRemindersEnabled(parsed.assignmentRemindersEnabled ?? true);
        setReminderDaysBefore(parsed.reminderDaysBefore ?? '2');
        setReminderHoursBefore(parsed.reminderHoursBefore ?? '12');
        setOverdueAlertsEnabled(parsed.overdueAlertsEnabled ?? true);
        setUrgentOnly(parsed.urgentOnly ?? false);
      } catch {}
    }
  }, []);

  useEffect(() => {
    setLocalAssignmentRemindersEnabled(assignmentRemindersEnabled ?? true);
  }, [assignmentRemindersEnabled]);

  const handleSave = () => {
    const settings = {
      assignmentRemindersEnabled: localAssignmentRemindersEnabled,
      reminderDaysBefore,
      reminderHoursBefore,
      overdueAlertsEnabled,
      urgentOnly,
    };

    localStorage.setItem('notificationSettings', JSON.stringify(settings));

    if (onSaveSettings) {
      onSaveSettings(settings);
    }
  };

  const handleReset = () => {
    setLocalAssignmentRemindersEnabled(true);
    setReminderDaysBefore(2);
    setReminderHoursBefore(12);
    setOverdueAlertsEnabled(true);
    setUrgentOnly(false);
  };

  if (!isOpen) return null;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <div>
          <div style={styles.title}>
            <Text weight="bold">Notification Settings</Text>
          </div>
        </div>

        <div style={styles.closeButtonWrapper}>
          <CloseButton
            placement="end"
            offset="none"
            screenReaderLabel="Close notification settings"
            onClick={onClose}
          />
        </div>
      </div>

      <div style={styles.settingsArea}>
        <div style={styles.section}>
          <Text weight="bold">Assignment Reminders</Text>
          <Text size="small" color="secondary">
            Choose when to be reminded about unfinished Canvas assignments.
          </Text>

          <label style={styles.toggleRow}>
            <span style={styles.labelText}>Enable assignment reminders</span>
            <input
              type="checkbox"
              checked={localAssignmentRemindersEnabled}
              onChange={(e) => setLocalAssignmentRemindersEnabled(e.target.checked)}
            />
          </label>

          <div style={styles.inputGroup}>
            <label style={styles.inputLabel}>Days before due date</label>
            <input
              type="number"
              min="0"
              value={reminderDaysBefore}
              onChange={(e) => setReminderDaysBefore(e.target.value)}
              onBlur={() => {
                if (reminderDaysBefore === '') {
                  setReminderDaysBefore('0');
                }
              }}
              style={styles.textInput}
              disabled={!localAssignmentRemindersEnabled}
            />
          </div>

          <div style={styles.inputGroup}>
            <label style={styles.inputLabel}>Hours before due date</label>
            <input
              type="number"
              min="0"
              max="23"
              value={reminderHoursBefore}
              onChange={(e) => setReminderHoursBefore(e.target.value)}
              onBlur={() => {
                if (reminderHoursBefore === '') {
                  setReminderHoursBefore('0');
                }
              }}
              style={styles.textInput}
              disabled={!localAssignmentRemindersEnabled}
            />
          </div>
        </div>

        <div style={styles.section}>
          <Text weight="bold">Other Notifications</Text>

          <label style={styles.toggleRow}>
            <span style={styles.labelText}>Alert when assignments is overdue</span>
            <input
              type="checkbox"
              checked={overdueAlertsEnabled}
              onChange={(e) => setOverdueAlertsEnabled(e.target.checked)}
            />
          </label>

          <label style={styles.toggleRow}>
            <span style={styles.labelText}>Only notify for urgent assignments</span>
            <input
              type="checkbox"
              checked={urgentOnly}
              onChange={(e) => setUrgentOnly(e.target.checked)}
            />
          </label>
        </div>
      </div>

      <div style={styles.actionArea}>
        <Button color="secondary" onClick={handleReset}>
          Reset
        </Button>
        <Button color="primary" onClick={handleSave}>
          Save
        </Button>
      </div>
    </div>
  );
}

const styles = {
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

  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    padding: '14px 16px',
    borderBottom: '1px solid #e6e9ec',
    background: '#f8f9fa',
  },

  title: {
    fontSize: '16px',
    color: '#1f2937',
  },

  closeButtonWrapper: {
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'center',
    position: 'relative',
  },

  settingsArea: {
    flex: 1,
    overflowY: 'auto',
    padding: '14px',
    background: '#fcfcfd',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },

  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: '5px',
    background: '#ffffff',
    border: '1px solid #e6e9ec',
    borderRadius: '12px',
    padding: '12px',
  },

  toggleRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '12px',
    fontSize: '14px',
  },

  labelText: {
    color: '#1f2937',
    flex: 1,
  },

  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },

  inputLabel: {
    fontSize: '13px',
    color: '#4b5563',
  },

  textInput: {
    width: '100%',
    border: '1px solid #c7cdd4',
    borderRadius: '8px',
    padding: '8px 10px',
    fontSize: '14px',
    fontFamily: 'inherit',
    boxSizing: 'border-box',
    background: '#ffffff',
  },

  timeRow: {
    display: 'flex',
    gap: '10px',
  },

  timeInputWrapper: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },

  actionArea: {
    borderTop: '1px solid #e6e9ec',
    background: '#ffffff',
    padding: '12px',
    display: 'flex',
    justifyContent: 'space-between',
    gap: '10px',
  },
};
