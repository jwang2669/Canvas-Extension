/**
 * WorkloadSummary.jsx
 * Fetches assignments from the Canvas API, scores them via the backend
 * workload endpoint, and displays a prioritized list with an AI summary.
 */

import React, { useState, useEffect } from 'react';
import { CloseButton, Text, Spinner } from '@instructure/ui';

const BACKEND_URL = 'http://localhost:8080';

const DAY_ORDER = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const SORT_OPTIONS = [
  { value: 'urgency', label: 'Urgency' },
  { value: 'weekday', label: 'Day of Week' },
  { value: 'minutes', label: 'Total Time' },
];

async function fetchWorkload() {
  const assignmentsRes = await fetch(`${BACKEND_URL}/api/canvas/assignments?week=current`);
  const { assignments } = await assignmentsRes.json();
  const tasks = assignments.map((a) => ({
    title: a.title,
    dueDate: a.dueDate,
    estimatedMinutes: a.estimatedMinutes,
  }));

  const response = await fetch(`${BACKEND_URL}/api/workload`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ tasks }),
  });

  if (!response.ok) throw new Error(`Server error: ${response.status}`);
  return response.json();
}

// Returns appropriate colors based on urgency score. Green for low, yellow for medium, red for high urgency.
function getUrgencyColors(score) {
  if (score >= 50)
    return { background: '#fef2f2', border: '#fecaca', value: '#991b1b', label: '#dc2626' };
  if (score >= 25)
    return { background: '#fefce8', border: '#fde68a', value: '#92400e', label: '#d97706' };
  return { background: '#f0fdf4', border: '#bbf7d0', value: '#14532d', label: '#16a34a' };
}

// Sorts the day entries based on the selected criteria: urgency, weekday order, or total minutes.
function sortDays(entries, sortBy, perDayMinutes) {
  const copy = [...entries];
  if (sortBy === 'urgency') {
    return copy.sort(([, a], [, b]) => b - a);
  }
  if (sortBy === 'weekday') {
    return copy.sort(([a], [b]) => DAY_ORDER.indexOf(a) - DAY_ORDER.indexOf(b));
  }
  if (sortBy === 'minutes') {
    return copy.sort(([a], [b]) => (perDayMinutes[b] ?? 0) - (perDayMinutes[a] ?? 0));
  }
  return copy;
}

export default function WorkloadSummary({ isOpen, onClose }) {
  const [workload, setWorkload] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [sortBy, setSortBy] = useState('urgency');

  useEffect(() => {
    if (!isOpen) return;
    setLoading(true);
    setError(null);
    fetchWorkload()
      .then((data) => setWorkload(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [isOpen]);

  if (!isOpen) return null;

  const sortedDays = workload
    ? sortDays(Object.entries(workload.weightedPerDay), sortBy, workload.perDayMinutes)
    : [];

  return (
    <div style={styles.container}>
      {/* Header */}
      <div style={styles.header}>
        <div>
          <div style={styles.title}>
            <Text weight="bold">Workload Summary</Text>
          </div>
        </div>
        <div style={styles.closeButtonWrapper}>
          <CloseButton
            placement="end"
            offset="none"
            screenReaderLabel="Close workload summary page"
            onClick={onClose}
          />
        </div>
      </div>

      {/* Sort toolbar */}
      <div style={styles.toolbar}>
        <Text size="small" color="secondary">
          Sort by
        </Text>
        <div style={styles.sortButtons}>
          {SORT_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              onClick={() => setSortBy(opt.value)}
              style={{
                ...styles.sortButton,
                ...(sortBy === opt.value ? styles.sortButtonActive : {}),
              }}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </div>

      {/* Body */}
      <div style={styles.scrollArea}>
        {loading && (
          <div style={styles.stateBlock}>
            <Spinner renderTitle="Loading assignments" size="medium" />
          </div>
        )}

        {!loading && error && (
          <div style={styles.errorBox}>
            <Text size="small" color="danger">
              {error}
            </Text>
          </div>
        )}

        {!loading && !error && workload && (
          <>
            {/* AI Summary */}
            {workload.summary && (
              <div style={styles.summaryBox}>
                <Text size="small">{workload.summary}</Text>
              </div>
            )}

            {/* Day list */}
            <ul style={styles.dayList}>
              {sortedDays.map(([day, score]) => {
                const urgencyColors = getUrgencyColors(score);
                return (
                  <li key={day} style={styles.dayCard}>
                    <div style={styles.dayBody}>
                      <Text weight="bold" size="small">
                        {day}
                      </Text>
                      <div style={styles.dayMeta}>
                        <Text size="small" color="secondary">
                          {workload.perDayMinutes[day] ?? 0} min
                        </Text>
                      </div>
                    </div>

                    <div
                      style={{
                        ...styles.scoreBadge,
                        background: urgencyColors.background,
                        border: `1px solid ${urgencyColors.border}`,
                      }}
                    >
                      <span style={{ ...styles.scoreValue, color: urgencyColors.value }}>
                        {score.toFixed(1)}
                      </span>
                      <span style={{ ...styles.scoreLabel, color: urgencyColors.label }}>
                        urgency
                      </span>
                    </div>
                  </li>
                );
              })}
            </ul>
          </>
        )}

        {!loading && !error && !workload && (
          <div style={styles.stateBlock}>
            <Text size="small" color="secondary">
              No assignments found.
            </Text>
          </div>
        )}
      </div>

      {/* Footer */}
      <div style={styles.inputArea}>
        {workload ? (
          <Text size="small" color="secondary">
            Total: {workload.totalMinutes} min · Avg: {Math.round(workload.totalMinutes / 7)}{' '}
            min/day
          </Text>
        ) : (
          <Text size="small" color="secondary">
            Oil on Canvas
          </Text>
        )}
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

  toolbar: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    padding: '8px 14px',
    borderBottom: '1px solid #e6e9ec',
    background: '#f8f9fa',
  },

  sortButtons: {
    display: 'flex',
    gap: '6px',
  },

  sortButton: {
    padding: '4px 5px',
    fontSize: '12px',
    borderRadius: '20px',
    border: '1px solid #d1d5db',
    background: '#ffffff',
    color: '#000000',
    cursor: 'pointer',
    fontFamily: 'Lato, "Helvetica Neue", Helvetica, Arial, sans-serif',
    transition: 'all 0.15s ease',
  },

  sortButtonActive: {
    background: '#fecaca',
    color: '#000000',
  },

  scrollArea: {
    flex: 1,
    overflowY: 'auto',
    padding: '14px',
    background: '#fcfcfd',
    display: 'flex',
    flexDirection: 'column',
    minHeight: 0,
  },

  stateBlock: {
    padding: '24px 8px',
    textAlign: 'center',
  },

  errorBox: {
    padding: '12px',
    background: '#fef2f2',
    border: '1px solid #fecaca',
    borderRadius: '10px',
  },

  summaryBox: {
    background: '#f0f4ff',
    border: '1px solid #c7d2fe',
    borderRadius: '10px',
    padding: '10px 12px',
    marginBottom: '12px',
    color: '#1f2937',
  },

  dayList: {
    margin: 0,
    padding: 0,
    listStyle: 'none',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },

  dayCard: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    gap: '12px',
    background: '#ffffff',
    border: '1px solid #e6e9ec',
    borderRadius: '10px',
    padding: '10px 14px',
    boxShadow: '0 1px 2px rgba(0, 0, 0, 0.04)',
  },

  dayBody: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: '2px',
    minWidth: 0,
  },

  dayMeta: {
    marginTop: '2px',
  },

  scoreBadge: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: '44px',
    height: '44px',
    borderRadius: '50%',
    flexShrink: 0,
  },

  scoreValue: {
    fontSize: '12px',
    fontWeight: 'bold',
    lineHeight: 1.1,
  },

  scoreLabel: {
    fontSize: '9px',
    lineHeight: 1.1,
  },

  inputArea: {
    borderTop: '1px solid #e6e9ec',
    background: '#ffffff',
    padding: '12px',
    display: 'flex',
    gap: '10px',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '44px',
    boxSizing: 'border-box',
  },
};
