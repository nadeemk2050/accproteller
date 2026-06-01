# AccPro Teller

A lightweight Android app for cashiers and bank tellers. Works as a companion to the main **AccPro** accounting system.

## Features

- **API Key setup** — Secure first-time configuration
- **Team login** — Only team members (cashiers/bankers) can log in
- **Payment, Receipt & Contra vouchers** — Create entries with DR/CR account selection
- **Cashier / Bank Balances** — Real-time balances view for cash and bank accounts

## Architecture

- **Kotlin + Jetpack Compose** — Modern declarative UI
- **Retrofit** — API calls to AccPro backend
- **DataStore** — Local persistence for API key, auth token & session
- **Navigation Compose** — Screen routing

## API Endpoints

The app calls these endpoints on the AccPro server:

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/teller/login` | POST | Team user authentication |
| `/api/teller/voucher` | POST | Create payment/receipt/contra |
| `/api/teller/balances` | GET | Fetch cash/bank balances |
| `/api/teller/accounts` | GET | Fetch account list |

## Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Wireless Debugging

1. Enable **Developer options** and **Wireless debugging** on your Android device
2. Pair with Android Studio or use `adb connect`
3. Run: `./gradlew installDebug`

## License

Private — AccPro internal tool.
