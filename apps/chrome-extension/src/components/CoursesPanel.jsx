/**
 * Lists Canvas courses for the token configured on the backend (active enrollment).
 * Panel chrome (size, header, footer bar, scroll shell) matches {@link Chatroom};
 * course rows are a readable list, not chat bubbles.
 */

import React, { useEffect, useState } from 'react';
import { CloseButton, Text } from '@instructure/ui';
import { BACKEND_BASE_URL } from '../config.js';

function formatDate(iso) {
  if (!iso) return null;
  try {
    const d = new Date(iso);
    return Number.isNaN(d.getTime()) ? iso : d.toLocaleDateString();
  } catch {
    return iso;
  }
}

export default function CoursesPanel({ isOpen, onClose }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [courses, setCourses] = useState([]);

  useEffect(() => {
    if (!isOpen) return;

    let cancelled = false;
    setLoading(true);
    setError(null);
    setCourses([]);

    fetch(`${BACKEND_BASE_URL}/api/canvas/courses`)
      .then(async (res) => {
        const text = await res.text();
        let data = null;
        try {
          data = text ? JSON.parse(text) : null;
        } catch {
          throw new Error('Invalid JSON from server');
        }
        if (!res.ok) {
          const msg = data?.message || data?.error || `${res.status} ${res.statusText}`;
          throw new Error(msg);
        }
        return data;
      })
      .then((data) => {
        if (cancelled) return;
        const list = Array.isArray(data?.courses) ? data.courses : [];
        setCourses(list);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message || 'Failed to load courses');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <div>
          <div style={styles.title}>
            <Text weight="bold">Courses</Text>
          </div>
          <Text size="small" color="secondary">
            Active enrollments from Canvas
          </Text>
        </div>

        <div style={styles.closeButtonWrapper}>
          <CloseButton
            placement="end"
            offset="none"
            screenReaderLabel="Close courses list"
            onClick={onClose}
          />
        </div>
      </div>

      <div style={styles.scrollArea}>
        {loading && (
          <div style={styles.stateBlock}>
            <Text color="secondary">Loading courses…</Text>
          </div>
        )}

        {!loading && error && (
          <div style={styles.errorBox}>
            <Text size="small" color="danger">
              {error}
            </Text>
          </div>
        )}

        {!loading && !error && courses.length === 0 && (
          <div style={styles.stateBlock}>
            <Text size="small" color="secondary">
              No courses returned. Check backend Canvas configuration.
            </Text>
          </div>
        )}

        {!loading && !error && courses.length > 0 && (
          <ul style={styles.courseList}>
            {courses.map((c) => (
              <li key={c.id} style={styles.courseCard}>
                <div style={styles.courseBody}>
                  <Text weight="bold" size="small" color="primary">
                    {c.name || `Course ${c.id}`}
                  </Text>
                  {c.courseCode && (
                    <div style={styles.courseCodeRow}>
                      <Text size="small" color="secondary">
                        {c.courseCode}
                      </Text>
                    </div>
                  )}
                  {(c.startAt || c.endAt) && (
                    <div style={styles.courseDates}>
                      <Text size="small" color="secondary">
                        {[formatDate(c.startAt), formatDate(c.endAt)].filter(Boolean).join(' → ')}
                      </Text>
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div style={styles.inputArea}>
        <Text size="small" color="secondary">
          Oil on Canvas
        </Text>
      </div>
    </div>
  );
}

/** Panel shell aligned with {@link Chatroom}; inner content is list UI */
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

  courseList: {
    margin: 0,
    padding: 0,
    listStyle: 'none',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },

  courseCard: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'stretch',
    background: '#ffffff',
    border: '1px solid #e6e9ec',
    borderRadius: '10px',
    overflow: 'hidden',
    boxShadow: '0 1px 2px rgba(0, 0, 0, 0.04)',
  },

  courseBody: {
    flex: 1,
    padding: '12px 14px',
    minWidth: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
  },

  courseCodeRow: {
    marginTop: '2px',
  },

  courseDates: {
    marginTop: '4px',
    paddingTop: '6px',
    borderTop: '1px solid #f0f2f4',
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
