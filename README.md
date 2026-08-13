# SpeakAPro 🎧🌍

**SpeakAPro** — Android-приложение для синхронного перевода окружающей речи прямо в наушники пользователя.

Главный сценарий: пользователь находится в другой стране, надевает Bluetooth/проводные наушники, нажимает **«Слушать»**, а телефон через свой микрофон улавливает речь окружающих и почти сразу воспроизводит перевод в наушниках.

> Статус: **MVP / начало разработки**

## Цель MVP

Сделать максимально простой и быстрый режим **«Слушаю»**:

```text
Человек рядом говорит
        ↓
Микрофон телефона
        ↓
Android AudioRecord
        ↓
PCM 16-bit / 16 kHz / mono
        ↓
Gemini Live API
Gemini 3.5 Live Translate
        ↓
PCM 16-bit / 24 kHz / mono
        ↓
Android AudioTrack
        ↓
Наушники пользователя
```

Параллельно приложение показывает:

- распознанный оригинальный текст;
- текст перевода;
- состояние соединения;
- активный язык перевода.

## AI-модель

MVP использует **Gemini 3.5 Live Translate Preview**:

```text
gemini-3.5-live-translate-preview
```

Модель специально предназначена для низколатентного speech-to-speech перевода и поддерживает более 70 языков.

Для Live Translation:

- вход: raw 16-bit PCM, 16 kHz, mono, little-endian;
- рекомендуемый размер аудиочанка: 100 ms;
- выход: raw 16-bit PCM, 24 kHz, mono, little-endian;
- исходный язык определяется моделью;
- задаётся целевой язык `targetLanguageCode`;
- можно получать входную и выходную транскрипцию.

## Технологии

### Android

- Kotlin
- Jetpack Compose
- Coroutines / Flow
- AudioRecord
- AudioTrack
- AudioManager
- Foreground Service для длительного прослушивания
- WebSocket client

### Backend

Backend не должен проксировать постоянный аудиопоток в production без необходимости.

Его основная задача на первом этапе:

1. хранить настоящий Gemini API key только на сервере;
2. выдавать Android-клиенту короткоживущий Gemini **ephemeral token**;
3. позже — авторизация, подписки, лимиты и аналитика использования.

После получения ephemeral token телефон подключается к Gemini Live API напрямую по WebSocket. Это уменьшает задержку и не раскрывает постоянный API key внутри APK.

## Архитектура проекта

Планируемая структура Android-приложения:

```text
app/
└── src/main/java/.../
    ├── MainActivity.kt
    ├── audio/
    │   ├── AudioRecorder.kt
    │   ├── AudioPlayer.kt
    │   └── AudioRouter.kt
    ├── live/
    │   ├── GeminiLiveClient.kt
    │   ├── LiveTranslateSession.kt
    │   └── LiveProtocol.kt
    ├── service/
    │   └── TranslationService.kt
    ├── ui/
    │   ├── TranslatorScreen.kt
    │   ├── LanguagePicker.kt
    │   └── SettingsScreen.kt
    └── state/
        └── TranslatorState.kt
```

Фактическую структуру разрешается корректировать во время реализации, но аудио, Live API, UI и foreground service должны оставаться разделёнными по ответственности.

## Первый пользовательский сценарий

1. Пользователь открывает SpeakAPro.
2. Даёт разрешение на микрофон.
3. Подключает наушники.
4. Выбирает язык, который хочет слышать, например `Русский`.
5. Нажимает **«Начать перевод»**.
6. Телефон слушает окружающую речь.
7. Переведённый звук воспроизводится в наушниках.
8. На экране отображаются оригинал и перевод.
9. Пользователь нажимает **«Остановить»** для завершения сессии.

## Принципы MVP

- **Минимальная задержка важнее количества функций.**
- Не добавлять регистрацию, социальные функции и сложную навигацию до рабочего real-time перевода.
- Не хранить постоянный Gemini API key в APK.
- Не сохранять аудиозаписи пользователя по умолчанию.
- Ошибка сети не должна зависать приложение: сессия должна корректно завершаться или переподключаться.
- UI должен оставаться понятным одной рукой и не требовать постоянного взаимодействия.
- Сначала стабилизировать режим «Слушаю», затем переходить к двустороннему разговору.

## Не входит в первый MVP

- iOS;
- офлайн-перевод;
- аккаунты и подписки;
- история аудиозаписей;
- групповые комнаты;
- распознавание конкретных собеседников;
- автоматический двусторонний режим разговора;
- облачное хранение транскрипций.

## Следующие этапы

После стабильного MVP планируются:

1. режим **«Разговор»** — собеседник → наушники, пользователь → динамик телефона;
2. push-to-talk для ответа пользователя;
3. локальный VAD и оптимизация трафика;
4. выбор голоса и скорости воспроизведения, если API/модель это позволяет;
5. offline fallback;
6. авторизация и подписки;
7. iOS-версия.

## Документация

- [Техническое задание](docs/TECH_SPEC.md)
- [TODO / Roadmap](TODO.md)

## Официальные источники

- Gemini Live Translation: https://ai.google.dev/gemini-api/docs/live-api/live-translate
- Gemini 3.5 Live Translate model: https://ai.google.dev/gemini-api/docs/models/gemini-3.5-live-translate-preview
- Live API WebSockets: https://ai.google.dev/gemini-api/docs/live-api/get-started-websocket
- Ephemeral tokens: https://ai.google.dev/gemini-api/docs/live-api/ephemeral-tokens
- Gemini API pricing: https://ai.google.dev/gemini-api/docs/pricing

---

**SpeakAPro — hear the world in your language.**
