# Research Report

## Chrome Extension Setup

### Summary of Work

I researched the steps necessary to create a chrome extension.

### Motivation

I was motivated to do this research because our app will be a chrome extension.

### Time Spent

I spent about 30 minutes doing preliminary research / looking at overviews online, another 30 minutes following a chrome extension developer tutorial, and another 30 minutes researching the best tools/configurations to use for our usage of a chrome extension.

### Results

I learned how to create a basic chrome extension by following the chrome developer guide[^1]. I found that a chrome extension consists of three parts:

1. Manifest: the manifest.json file that defines the project's metadata, permissions, and registers scripts/content
2. Background service worker: A process that runs in the background and handles browser events
3. Content script: A script that runs your javascript within the web page you are using the extension on.
   Following this guide I produced a minimal chrome extension for practice.

Next, I researched common tools or configurations people use when actually building chrome extensions in the real world. I read a blog post on "How to Create a Chrome Extension with React, TypeScript, TailwindCSS, and Vite"[^2]. After reading it, I decided our project should use React and Vite with the CRXJS plugin, but not Typescript or TailwindCSS.

After reading this, I began making our starter files for a chrome extension, and configured it with React and Vite.

I began by creating a new folder /chrome-extension and installing vite.

````cd chrome-extension
npm init vite@latest
# Choose "vanilla" (JS)
npm install
npm install @crxjs/vite-plugin --save-dev ```

This creates a basic /src structure with an index.html.
Next I created other necessary files:
    background.js -  my background/service worker
    content.js - content script
    popup.js - popup logic
    popup.html - popup UI
  manifest.json

I stopped here with just the outline, before fully configuring the manifest.json and vite.config.js files.


### Sources

[^1]: https://developer.chrome.com/docs/extensions/get-started
[^2]: https://www.luckymedia.dev/blog/how-to-create-a-chrome-extension-with-react-typescript-tailwindcss-and-vite-in-2024

````
