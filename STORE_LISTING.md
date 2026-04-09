# 🏪 Time Flow — Google Play Store Listing Assets

Use this document to fill out your Play Console store listing.

---

## 📋 App Details

| Field | Value |
|---|---|
| **App name** | Time Flow |
| **Application ID** | com.timeflow.app |
| **Default language** | English (United States) |
| **Category** | Productivity |
| **Content rating** | Everyone |
| **Email** | support@timeflow.app |
| **Privacy Policy URL** | *(host the HTML below at e.g. https://timeflow.app/privacy)* |

---

## 📝 Short Description (max 80 chars)

```
Elegant countdown timers with widgets, notifications & yearly repeats.
```

---

## 📝 Full Description (max 4000 chars)

```
⏳ Time Flow — Beautiful Countdown Timers

Never miss a moment that matters. Time Flow lets you create stunning, colorful countdown timers for every occasion — birthdays, holidays, travel, product launches, and more.

✨ KEY FEATURES

🎨 Beautiful Themes
Choose from Classic (warm sandy tones) or Minimal (clean monochrome) themes that adapt perfectly to light and dark mode.

⏰ Smart Countdowns
Create countdowns to any future date and time. Set events to repeat yearly for birthdays and anniversaries — Time Flow automatically advances them each year.

🔔 Exact Notifications
Get notified the moment your countdown reaches zero, even when your phone is asleep.

🗂️ Categories & Search
Organize timers by category (Personal, Work, Health, Travel, and more). Search instantly by title or category.

📌 Home Screen Widget
Pin any countdown directly to your home screen. Choose from transparent, color-matched, or image backgrounds. Customize text size to fit your style.

🖼️ Custom Images
Add a photo to each countdown card to make it personal — perfect for trips, weddings, or big life moments.

📦 Bulk Actions
Select multiple countdowns to delete, archive, or recategorize them all at once.

🌗 Dark Mode
Fully supports system-level dark mode with carefully tuned colors for every theme.

🔒 100% Private
All your data lives on your device. No accounts, no cloud sync, no ads, no tracking — ever.

─────────────────────────────
Whether you're counting down to a wedding, a launch day, a birthday, or simply the weekend — Time Flow helps you feel the anticipation.

Download Time Flow and start counting!
```

---

## 🖼️ Required Graphic Assets

| Asset | Size | Notes |
|---|---|---|
| **App icon** | 512×512 px PNG | High-res version of `countdown_app_icon.png` |
| **Feature graphic** | 1024×500 px JPG/PNG | Used at top of Play listing — design tip: show the app on a phone with a colorful countdown card |
| **Phone screenshots** | 2–8 screenshots, min 320px wide | See shot list below |
| **Tablet screenshots** | Optional (7-in & 10-in) | Recommended for better ranking |

### 📸 Recommended Screenshot List

1. **Home screen (Active tab)** — Show 3–4 colorful countdown cards
2. **Countdown card details** — Tap to open a card with image and days/hours/mins/secs
3. **Add Countdown dialog** — Show the create form with a color picker and repeat toggle
4. **Dark mode home screen** — Same as #1 but in dark theme
5. **Home screen widget** — Lock screen or home screen with the widget on display
6. **Side menu open** — Drawer showing Theme and Preferences toggles
7. **Search / filter** — Search bar with results filtered by category

### 🎨 Feature Graphic Tips

- Use a warm gradient (sand/orange tones) that matches the Classic theme
- Place a phone mockup showing the app over the gradient
- Add the app name "Time Flow" in large bold text
- Tagline: *"Count every moment."*

---

## 📦 Release Checklist

- [ ] Upload `app-release.apk` or `app-release.aab` (AAB preferred)
- [ ] Set content rating questionnaire (Productivity → Everyone)
- [ ] Add privacy policy URL to Play Console
- [ ] Fill store listing with assets above
- [ ] Set price: Free
- [ ] Enable countries: Worldwide
- [ ] Review & submit for review

---

## 🔑 Keystore Info (keep secret — never commit to git)

| Field | Value |
|---|---|
| Keystore file | `app/timeflow-release.keystore` |
| Key alias | `timeflow` |
| Credentials file | `keystore.properties` (gitignored) |

> ⚠️ **Back up `timeflow-release.keystore` and `keystore.properties` to a secure location (e.g. encrypted cloud storage). If you lose the keystore, you cannot update the app on Play Store.**

---

## 🌐 Privacy Policy Hosting

Host the following HTML at your domain (e.g. `https://timeflow.app/privacy`):

```html
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Privacy Policy – Time Flow</title>
<style>
  body { font-family: sans-serif; max-width: 700px; margin: 40px auto; padding: 0 20px; color: #333; line-height: 1.7; }
  h1 { color: #1565C0; }
  h2 { color: #1976D2; margin-top: 28px; }
</style>
</head>
<body>
<h1>Privacy Policy – Time Flow</h1>
<p><strong>Last updated:</strong> April 9, 2026</p>
<p>Time Flow ("the App") is developed by Chethan S ("Developer").</p>

<h2>1. Data We Collect</h2>
<p>Time Flow stores all countdown data locally on your device using a Room (SQLite) database. We do not collect, transmit, or share any personal data with third parties.</p>

<h2>2. Notifications</h2>
<p>The App requests the <code>POST_NOTIFICATIONS</code> permission solely to notify you when a countdown finishes. No notification data leaves your device.</p>

<h2>3. Alarms</h2>
<p>The App uses <code>SCHEDULE_EXACT_ALARM</code> / <code>USE_EXACT_ALARM</code> to fire notifications at your chosen countdown time. All processing is on-device.</p>

<h2>4. Images</h2>
<p>Photos added to countdowns are stored in the App's private internal storage and are never uploaded or shared.</p>

<h2>5. Analytics &amp; Advertising</h2>
<p>The App contains no analytics SDKs, advertising SDKs, or tracking of any kind.</p>

<h2>6. Children</h2>
<p>The App does not knowingly collect data from children under 13.</p>

<h2>7. Contact</h2>
<p>Questions? Email: <a href="mailto:support@timeflow.app">support@timeflow.app</a></p>
</body>
</html>
```

---

*Generated: April 9, 2026*

