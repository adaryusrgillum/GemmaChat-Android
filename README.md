# GemmaChat

An Android chat client for [Ollama](https://ollama.com) — connect to your locally running AI models (like Google's Gemma 4) from your phone.

## Features

- 💬 **Real-time streaming chat** with Google's Gemma 4 (or any Ollama model)
- 🎨 **Clean Material Design 3 UI** with chat bubbles
- ⚙️ **Configurable server URL & model name** in Settings
- 📡 **LAN connectivity** — talk to Ollama running on your PC over WiFi

## Screenshots

*Chat UI with user messages (purple, right) and assistant replies (white, left)*

## Requirements

- Android 8.0+ (API 26+)
- A PC on the same WiFi network running [Ollama](https://ollama.com)
- At least one model pulled in Ollama (e.g., `gemma4`)

## Setup

### 1. Install Ollama & pull Gemma 4

```bash
# On your PC
ollama pull gemma4
ollama list
```

### 2. Expose Ollama to your LAN

Ollama defaults to `localhost:11434` (PC-only). Make it reachable from your phone:

```bash
# Windows PowerShell
$env:OLLAMA_HOST = "0.0.0.0:11434"
ollama serve
```

Or set permanently:
```powershell
[Environment]::SetEnvironmentVariable("OLLAMA_HOST", "0.0.0.0:11434", "User")
```

> ⚠️ Make sure Windows Firewall allows incoming connections on port `11434`.

### 3. Find your PC's local IP

```powershell
ipconfig
# Look for: IPv4 Address . . . . . . : 192.168.1.xxx
```

### 4. Install GemmaChat APK

Download the latest release APK and install it on your Android device.

### 5. Configure & Chat

1. Open **GemmaChat**
2. Tap **⋮ (menu) → Settings**
3. Set **Ollama Server URL** to your PC's IP:
   - Real phone: `http://192.168.1.xxx:11434`
   - Android Emulator: `http://10.0.2.2:11434` (default)
4. Set **Model Name** to `gemma4` (or whichever model you want)
5. Go back and start chatting!

## Architecture

- **OkHttp** for HTTP streaming (NDJSON)
- **Ollama `/api/chat` endpoint** with streaming enabled
- **AndroidX Preference** for settings storage
- **RecyclerView** with dual-view-type adapter for chat bubbles

## Building from Source

```bash
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## License

MIT
