# WebView & Fetch Guide

The WebView is a powerful tool in IReader that allows you to browse sources directly and bypass anti-bot protections like Cloudflare.

## What is "Fetch"?

Some websites use strong protections (like Cloudflare) that prevent apps from loading chapters normally. The **Fetch** feature allows IReader to:
1.  Open the page in a WebView (like a browser).
2.  Wait for you to pass the protection (e.g., solve a CAPTCHA).
3.  Extract the novel content directly from the loaded page.

## How to Use Fetch

1.  **Open WebView**:
    *   If a chapter fails to load, tap the **WebView** button (usually a globe icon) in the error message or menu.
    *   Alternatively, long-press a book cover and select **Open in WebView**.
2.  **Pass Protection**:
    *   If you see a Cloudflare "Verify you are human" check, click it.
    *   Wait for the page to load completely.
3.  **Tap Fetch**:
    *   Once the chapter content is visible on the screen, tap the **Fetch** button (usually at the bottom or top right).
    *   IReader will parse the HTML and save the content.

## Auto-Fetch

IReader can automatically detect novel content in the WebView and fetch it.

*   **Enable**: Go to **Settings** → **Advanced** → **WebView** and enable **Auto-Fetch**.
*   **Usage**: When you navigate to a chapter page in WebView, IReader will attempt to grab the content automatically without you pressing the button.

## Troubleshooting

*   **"Failed to fetch"**: Ensure the page is fully loaded before tapping Fetch.
*   **Cloudflare Loop**: If Cloudflare keeps asking you to verify, try clearing cookies in **Settings** → **Advanced** → **Clear Cookies**.
