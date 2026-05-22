# Terms of Service and Privacy Policy

### Privacy Policy
I take your privacy very seriously. For standard local operation, **Axe** does not collect, track, or centralize any user data. All sensitive configuration details are securely stored locally within the application database on your device.

### App Permissions
To provide its core functionality, **Axe** requires the following device permissions:
*   **Usage Access:** To detect the currently running foreground application.
*   **Notification Access:** To retrieve active media playback details.
*   **Storage Access:** To save and import your custom configurations.

### Third-Party Services
This policy does not apply to third-party services you integrate with **Axe** (such as Discord). Please review the respective privacy policies on their official websites.

---

### Axe 24/7 Remote Gateway

The **Axe 24/7 Remote Gateway** is a strictly optional, opt-in feature designed to provide a continuous, reliable Discord Rich Presence (RPC) service. **By default, this feature is completely disabled, and all of your data remains 100% local to your device.**

The gateway will only activate if you manually toggle it on and explicitly confirm your choice within the application. By enabling this feature, you acknowledge and agree to the following:

*   **Data Transmission:** The application will securely transmit your **Discord User Token**, Discord User ID, and relevant RPC data to the external Axe Gateway server.
*   **Volatile In-Memory Lifecycle:** To prevent data breaches, **your User Token is never written to disk or stored in a persistent database.** It exists strictly in volatile system memory (RAM) and adheres to a strict automated cleanup lifecycle:
    *   **Active Sessions:** An active session is capped at a maximum duration of 48 hours.
    *   **Heartbeat Resets:** The Axe app sends a heartbeat ping every 4 hours, which automatically refreshes the 48-hour session window.
    *   **Automated Purging:** If the app fails to send a heartbeat within its expected window, the gateway automatically and permanently wipes your token and data from its memory.
    *   **Manual Deactivation:** Turning off the RPC feature or toggling the gateway off sends an immediate kill signal, instantly wiping your data from the server's memory.
*   **Open Source Transparency:** The full source code for the gateway is entirely transparent and open for public audit. You can review the deployment and memory management logic at the official [axe-server](https://github.com/sudoloser/axe-server) repository.

> ⚠️ **Important Security & Liability Disclaimer:** 
> Providing your Discord User Token carries inherent security risks, and utilizing custom RPC tools may technically violate Discord's Terms of Service. By using the Remote Gateway, you assume full responsibility for the security of your account and agree that the developers of Axe are not liable for any account restrictions, bans, or data compromises.

---

### Open Source License
**Axe** is an open-source project distributed under the **GNU GPL 3.0 License**. This allows you to freely use, reference, and modify the source code. However, any modified or derived versions of this software *cannot* be distributed or sold as closed-source commercial software. For full details, please refer to the complete GNU GPL 3.0 License text.

---
**Last Updated:** 5/22/26 (MM/DD/YY)

### Changelog
*   **5/22/26:** Introduced the Axe 24/7 Remote Gateway feature, including automated in-memory lifecycles and explicit user opt-in requirements.
*   **5/06/26:** Project rebranded from Kizzy to Axe. Full credit for the foundational codebase goes to the original creator, [dead8309](https://github.com/dead8309).
*   
