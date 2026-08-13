# SpeakAPro — техническое задание

Версия: **0.2**

## Цель

SpeakAPro — Android-приложение для потокового перевода окружающей речи прямо в наушники пользователя.

## Архитектура MVP

В первой версии **нет собственного сервера и нет арендуемого VPS**.

```text
Микрофон телефона
    ↓
AudioRecord
    ↓
Gemini Live API
    ↓
AudioTrack
    ↓
Наушники
```

Телефон работает с Gemini напрямую. Мы не используем NestJS, Docker, PostgreSQL, собственный WebSocket-сервер или проксирование аудио.

## Android

- Kotlin
- Jetpack Compose
- Coroutines / Flow
- ViewModel + StateFlow
- AudioRecord
- AudioTrack
- AudioManager
- WebSocket client
- Foreground Service после стабилизации основного режима

## Listen Mode

Пользователь подключает наушники, выбирает язык и нажимает `Начать перевод`.

Приложение:

1. слушает микрофон телефона;
2. формирует небольшие PCM-чанки;
3. отправляет аудио прямо в Gemini Live;
4. получает translated audio;
5. сразу воспроизводит перевод в наушниках;
6. показывает оригинальную и переведённую транскрипции.

## Gemini Live

Стартовая модель:

```text
gemini-3.5-live-translate-preview
```

Model ID и endpoint должны задаваться через конфигурацию.

Компонент `GeminiLiveClient` отвечает за соединение, session setup, отправку аудио, получение перевода, транскрипции, reconnect и корректное закрытие.

## AudioRecorder

- mono;
- PCM 16-bit;
- sample rate согласно Live API;
- запись вне UI thread;
- небольшие чанки, стартовая точка около 100 ms;
- raw audio не сохраняется.

## AudioPlayer

- AudioTrack streaming mode;
- bounded playback queue;
- отдельный playback loop;
- корректный stop/flush;
- очередь не должна бесконечно расти.

## Audio routing

Приоритет вывода:

1. Bluetooth-наушники;
2. проводные/USB-наушники;
3. динамик только как fallback.

## Состояния

```text
Idle → Connecting → Ready → Listening → Stopping → Idle
```

Ошибки: permission, configuration, network, Gemini, audio input, audio output.

## Локальная разработка

Данные доступа к Gemini хранятся только локально на машине разработчика, например через `local.properties`, и не коммитятся в GitHub.

## Публичный релиз

Защиту доступа решаем **после рабочего MVP**. Предпочтение — варианту без собственного постоянно работающего сервера: Google/Firebase client gateway с App Check, если он подходит выбранной Live-модели. Если потребуется временный токен, допускается маленькая serverless-функция только для его выдачи.

Арендованный VPS не является частью архитектуры SpeakAPro и рассматривается только если когда-нибудь появится отдельная необходимость.

## Foreground Service

После рабочего прототипа добавить длительное прослушивание с постоянным уведомлением и кнопкой Stop.

## Privacy

- raw audio не сохраняется;
- собственной базы данных в MVP нет;
- пользователь видит, когда микрофон активен;
- до публикации требуется корректная privacy policy.

## Не входит в MVP

- собственный backend;
- VPS;
- база данных;
- регистрация;
- подписки;
- iOS;
- offline translation;
- облачная история;
- автоматический двусторонний разговор.

## Порядок разработки

1. Android/Compose skeleton.
2. AudioRecord.
3. AudioTrack.
4. Headphone routing.
5. Прямое подключение к Gemini Live.
6. End-to-end Listen Mode.
7. Transcriptions и UI states.
8. Foreground Service.
9. Latency/stability tests.
10. Production security после успешного MVP.

## Definition of Done

MVP считается готовым, когда физический Android-телефон без собственного сервера стабильно слушает речь, отправляет её в Gemini, получает перевод, воспроизводит его в наушниках и корректно переживает Stop/Start и потерю сети.
