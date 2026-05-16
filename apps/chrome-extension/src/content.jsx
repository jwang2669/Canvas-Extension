/**
 * This is the Chrome extension's content script, which is automatically run when the extension is activated.
 * It mounts FloatingIcon.jsx onto the webpage, and provides the InstructureUI theme context to the entire extension.
 *
 * See https://instructure.design
 */

import React from 'react';
import { createRoot } from 'react-dom/client';
import { InstUISettingsProvider } from '@instructure/ui';
import Icon from './components/FloatingIcon.jsx';

if (!document.getElementById('oil-on-canvas-root')) {
  const host = document.createElement('div');
  host.id = 'oil-on-canvas-root';
  document.body.appendChild(host);

  const root = createRoot(host);
  root.render(
    //provide the InstUI theme context to the entire extension
    <InstUISettingsProvider theme={{ colorScheme: 'canvas' }}>
      <Icon />
    </InstUISettingsProvider>,
  );

  console.log('Oil On Canvas extension mounted');
}
