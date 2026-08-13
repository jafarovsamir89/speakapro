# TODO

## Implemented

- [x] Android project: Kotlin + Jetpack Compose
- [x] 16 kHz mono PCM microphone capture in 100 ms frames
- [x] Microphone permission flow and live level meter
- [x] 24 kHz PCM playback with `AudioTrack`
- [x] Gemini Live WebSocket client
- [x] Gemini Live Translate session setup
- [x] Stream microphone audio to Gemini
- [x] Play translated audio as it arrives
- [x] Show original and translated transcription
- [x] Target-language selector
- [x] Dark travel-first Listen Mode UI
- [x] Audio routing layer for phone microphone and headphones

## Next validation

- [ ] Add `GEMINI_API_KEY` locally and run on a physical Android phone
- [ ] Verify English -> Russian live translation
- [ ] Verify Azerbaijani / Turkish / Russian target languages
- [ ] Verify Bluetooth A2DP headphones
- [ ] Verify LE Audio headphones
- [ ] Verify wired / USB headphones
- [ ] Measure speech-to-translated-audio latency
- [ ] Test 15, 30 and 60 minute sessions

## Next development

- [ ] Hot-swap active audio route when headphones connect/disconnect mid-session
- [ ] Foreground microphone service for screen-off listening
- [ ] Conversation mode (two-way / push-to-talk)
- [ ] Language catalogue and search
- [ ] Settings screen
- [ ] Translation history (opt-in only)
- [ ] Production-safe Gemini access without a permanent key in the APK
- [ ] Internal beta
