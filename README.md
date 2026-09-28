# Launcher OS 27 — Step 9 Monetization

## Monetization status
- AdMob SDK integrated with Google test App ID and test ad units.
- Free users see an adaptive banner and an interstitial at a controlled frequency.
- Interstitial eligibility is persisted across launcher Activity recreation/restarts: every 8 eligible app launches, with a 2-minute cooldown.
- Premium users bypass both banner and interstitial ads.
- Google Play Billing 9.1.0 integration is present but disabled until the Play Console app/subscriptions exist.
- Test Premium can be activated/reset locally while billing is disabled.
- Monthly product ID: `launcher_premium_monthly`
- Yearly product ID: `launcher_premium_yearly`
- Billing entitlement is rechecked when returning to the launcher.

## Before production
1. Create the app in Google Play Console.
2. Create the two subscription products/base plans with the IDs above.
3. Change `SubscriptionManager.PLAY_BILLING_ENABLED` to `true`.
4. Replace the Google test AdMob App ID in `AndroidManifest.xml` with the production App ID.
5. Replace `AdsManager.BANNER_TEST_ID` and `INTERSTITIAL_TEST_ID` with production ad-unit IDs.
6. Test purchases with Play license testers/test tracks before release.
7. Never ship test ad IDs in a production build.


## Step 15
- Added persistent widget ordering controls (up/down).
- Added reset widget order action.
- Widget enable/disable state remains persistent.
- Existing launcher, weather, calendar, ads and subscription architecture preserved.


## Step 16
- Home page indicator upgraded to a compact iOS-style glass capsule.
- Active page remains a wider capsule; inactive pages remain translucent dots.
- Existing pager, widgets, dock, customization, ads and subscription behavior preserved.
- This source package has not been compiled into an APK in this environment.


## Step 29 — Play Store and release preparation
- Version metadata prepared as 1.0.0 with versionCode 1 for the first production release.
- Added `PLAY_STORE_LISTING.md` with store copy and required asset checklist.
- Added `RELEASE_NOTES_V1.md`.
- Permanent latest APK URL is documented in `PLAY_STORE_CHECKLIST.md`.
- Production AdMob IDs must replace the Google test App ID/test ad units before production release.
- Google Play Billing remains disabled until the Play Console products are created and tested.
