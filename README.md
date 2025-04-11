# IPS ATAK Plugin

The IPS ATAK Plugin is an Android Tactical Assault Kit (ATAK) plugin designed to integrate International Patient Summary (IPS) records into the ATAK ecosystem. It works in tandem with a MERN stack IPS webapp to receive and decode custom CoT (Cursor on Target) messages containing IPS data. The plugin allows users to view IPS data in either a JSON format or a custom, human-friendly layout and to trigger additional workflows such as fetching a webpage based on the IPS packageUUID.

## Features

- **CoT Message Integration:**  
  Receives CoT messages containing IPS data embedded in a custom `<ipsData>` element (gzipped and Base64 encoded).

- **Dual Display Options:**  
  Allows users to toggle between a nicely formatted JSON display and a custom plain text view with a simplified layout (no brackets or unwanted keys).

- **Map Item Selection:**  
  Provides an in-built map item inspection tool that lets users select a map item on ATAK, automatically extracting the IPS data from the associated CoT message.

- **External Web Integration:**  
  Enables triggering a GET request to a browser endpoint (e.g., `http://localhost:5000/tak/browser/:packageUUID`) based on the IPS record, displaying the result in a custom-styled AlertDialog with a WebView.

- **Custom UI Enhancements:**  
  Rounded and properly spaced buttons, configurable dialogs, and adjustable WebView settings ensure a user-friendly interface in both ATAK and WinTAK environments.

## Requirements

- **ATAK-CIV:**  
  The plugin is built against the ATAK-CIV developer version.

- **Android Studio:**  
  Use Android Studio with a minimum Android SDK of API Level 26 for full support of the latest APIs.

- **MERN Stack IPS Webapp:**  
  A companion MERN web application that compresses and sends IPS records within CoT messages.

- **USB Debugging:**  
  An Android device with USB debugging enabled and connected to your development machine.

## Installation and Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/yourusername/ips-atak-plugin.git
   cd ips-atak-plugin