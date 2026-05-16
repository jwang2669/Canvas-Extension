/**
 * FloatingIcon.jsx
 * This component renders the floating icon button, the main entry point for the chrome extension.
 * When clicked, it reveals a menu to navigate to different features.
 */

import React, { useState, useEffect } from 'react';
import { Button, IconButton } from '@instructure/ui';
import Chatroom from './Chatroom';
import WorkloadSummary from './WorkloadSummary';
import CoursesPanel from './CoursesPanel';
import NotificationSettings from './NotificationSettings';
import NotificationBadge from './NotificationBadge';
import { BACKEND_BASE_URL } from '../config.js';

function getExtensionLogoUrl() {
  try {
    const url = chrome.runtime.getURL('logo.png');
    return url && url.length > 0 ? url : null;
  } catch {
    return null;
  }
}

async function parseJsonResponse(res) {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

function pickCanvasDisplayName(user) {
  if (!user || typeof user !== 'object') return null;
  const n = user.shortName || user.short_name || user.name || user.loginId || user.login_id;
  const s = typeof n === 'string' ? n.trim() : '';
  return s.length > 0 ? s : null;
}

export default function FloatingIcon() {
  /** Never start as ""; React warns on img src="" and may reload the document. */
  const [logoUrl] = useState(getExtensionLogoUrl);
  const [menuOpen, setMenuOpen] = useState(false);
  const [chatOpen, setChatOpen] = useState(false);
  const [priorityOpen, setPriorityOpen] = useState(false);
  const [coursesOpen, setCoursesOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [assignmentRemindersEnabled, setAssignmentRemindersEnabled] = useState(true);
  const [badgeDismissed, setBadgeDismissed] = useState(false);
  /** From backend after Canvas courses + DB sync; sent with chat for personalization. */
  const [canvasSessionId, setCanvasSessionId] = useState(null);
  /** Display name from Canvas profile (same access token as /courses). */
  const [canvasUserDisplayName, setCanvasUserDisplayName] = useState(null);

  // Load courses (session + cache) and Canvas profile GET /users/self via backend.
  useEffect(() => {
    let cancelled = false;

    const loadCourses = fetch(`${BACKEND_BASE_URL}/api/canvas/courses`)
      .then(parseJsonResponse)
      .then((data) => {
        if (cancelled || !data) return;
        setCanvasSessionId(data.sessionId ?? null);
      })
      .catch(() => {
        if (!cancelled) setCanvasSessionId(null);
      });

    const loadProfile = fetch(`${BACKEND_BASE_URL}/api/canvas/me`)
      .then(parseJsonResponse)
      .then((data) => {
        if (cancelled) return;
        setCanvasUserDisplayName(pickCanvasDisplayName(data?.user));
      })
      .catch(() => {
        if (!cancelled) setCanvasUserDisplayName(null);
      });

    const savedSettings = localStorage.getItem('notificationSettings');
    if (savedSettings) {
      try {
        const parsed = JSON.parse(savedSettings);
        setAssignmentRemindersEnabled(parsed.assignmentRemindersEnabled ?? true);
      } catch {
        setAssignmentRemindersEnabled(true);
      }
    }

    Promise.all([loadCourses, loadProfile]).catch(() => {});

    return () => {
      cancelled = true;
    };
  }, []);

  const handleIconClick = () => {
    if (chatOpen) {
      setChatOpen(false);
      setMenuOpen(true);
      return;
    }
    if (coursesOpen) {
      setCoursesOpen(false);
      setMenuOpen(true);
      return;
    }
    if (priorityOpen) {
      setPriorityOpen(false);
      setMenuOpen(true);
      return;
    }
    if (notificationsOpen) {
      setNotificationsOpen(false);
      setMenuOpen(true);
      return;
    }
    setMenuOpen((prev) => {
      const next = !prev;
      if (next) {
        setBadgeDismissed(false);
      }
      return next;
    });
  };

  const refreshCanvasSession = async () => {
    try {
      const [coursesRes, meRes] = await Promise.all([
        fetch(`${BACKEND_BASE_URL}/api/canvas/courses`),
        fetch(`${BACKEND_BASE_URL}/api/canvas/me`),
      ]);
      const coursesData = await parseJsonResponse(coursesRes);
      const meData = await parseJsonResponse(meRes);
      if (coursesRes.ok && coursesData?.sessionId) {
        setCanvasSessionId(coursesData.sessionId);
      }
      if (meRes.ok) {
        setCanvasUserDisplayName(pickCanvasDisplayName(meData?.user));
      }
    } catch {
      // leave existing sessionId / name
    }
  };

  const handleOpenChat = () => {
    setMenuOpen(false);
    setCoursesOpen(false);
    setChatOpen(true);
    setNotificationsOpen(false);
    setPriorityOpen(false);
    refreshCanvasSession();
  };

  const handleOpenCourses = () => {
    setMenuOpen(false);
    setCoursesOpen(true);
    setChatOpen(false);
    setNotificationsOpen(false);
    setPriorityOpen(false);
  };

  const handleOpenPriority = () => {
    setMenuOpen(false);
    setCoursesOpen(false);
    setChatOpen(false);
    setNotificationsOpen(false);
    setPriorityOpen(true);
  };

  const handleOpenNotifications = () => {
    setMenuOpen(false);
    setChatOpen(false);
    setCoursesOpen(false);
    setPriorityOpen(false);
    setNotificationsOpen(true);
  };

  const handleCloseChat = () => {
    setChatOpen(false);
  };

  const handleClosePriority = () => {
    setPriorityOpen(false);
  };

  const handleCloseCourses = () => {
    setCoursesOpen(false);
  };

  const handleCloseNotifications = () => {
    setNotificationsOpen(false);
  };

  const handleSaveNotificationSettings = (settings) => {
    setAssignmentRemindersEnabled(settings.assignmentRemindersEnabled);

    if (!settings.assignmentRemindersEnabled) {
      setBadgeDismissed(true);
    } else {
      setBadgeDismissed(false);
    }
  };

  return (
    <>
      <div style={styles.wrapper}>
        {menuOpen && assignmentRemindersEnabled && !badgeDismissed && (
          <div style={styles.notificationPopupWrapper}>
            <NotificationBadge
              isOpen={true}
              onClose={() => setBadgeDismissed(true)}
              title="ASSIGNMENT NAME"
              course="COURSE101: Course Name (001)"
              dueText="Feb 25 at 11pm"
              alertText="ALERT: Assignment Due in 2 hours."
              onTitleClick={handleOpenPriority}
            />
          </div>
        )}

        {/* placeholder feature panel. Replace onClick with appropriate action later */}
        {/* Consider replacing panel with an InstUI component */}
        {menuOpen && !chatOpen && !coursesOpen && !priorityOpen && !notificationsOpen && (
          <div style={styles.panel}>
            <Button onClick={handleOpenChat}>Chat</Button>
            <Button onClick={handleOpenCourses}>Courses</Button>
            <Button onClick={handleOpenPriority}>Workload</Button>
            <Button onClick={handleOpenNotifications}>Notifications</Button>
          </div>
        )}
        {/* our Floating Icon Button, rendered using InstUI IconButton */}
        <IconButton
          onClick={handleIconClick}
          shape={'circle'}
          renderIcon={() =>
            logoUrl ? (
              <img
                src={logoUrl}
                alt="Open Oil on Canvas"
                width={44}
                height={44}
                style={{ display: 'block', objectFit: 'cover', borderRadius: '50%' }}
              />
            ) : null
          }
        />
      </div>

      <Chatroom
        isOpen={chatOpen}
        onClose={handleCloseChat}
        sessionId={canvasSessionId}
        userDisplayName={canvasUserDisplayName}
      />
      <CoursesPanel isOpen={coursesOpen} onClose={handleCloseCourses} />
      <WorkloadSummary isOpen={priorityOpen} onClose={handleClosePriority} />
      <NotificationSettings
        isOpen={notificationsOpen}
        onClose={handleCloseNotifications}
        assignmentRemindersEnabled={assignmentRemindersEnabled}
        onSaveSettings={handleSaveNotificationSettings}
      />
    </>
  );
}

const styles = {
  //Styles the container that the icon lives in.
  //  fixes it to the bottom right of the page, above all other content.
  wrapper: {
    position: 'fixed',
    bottom: '24px',
    right: '24px',
    zIndex: 2147483647,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: '10px',
    fontFamily: 'sans-serif',
  },
  panel: {
    background: '#fff',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
    padding: '12px',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    minWidth: '160px',
  },
  notificationPopupWrapper: {
    marginBottom: '8px',
    alignSelf: 'flex-end',
  },
};
