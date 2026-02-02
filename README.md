# CustomJoinMessage ✨

[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-2ea44f?logo=minecraft)](https://papermc.io)
[![Version](https://img.shields.io/badge/version-1.3.0-blue.svg)](https://github.com/Nixend-creator/CustomJoinMessage/releases)
[![License: CC BY-NC-ND 4.0](https://img.shields.io/badge/License-CC%20BY--NC--ND%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-nd/4.0/)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net)
[![Author: Nixend](https://img.shields.io/badge/Author-Nixend-ff69b4)](https://github.com/Nixend-creator)
[![Downloads](https://img.shields.io/github/downloads/Nixend-creator/CustomJoinMessage/total.svg)](https://github.com/Nixend-creator/CustomJoinMessage/releases)

---

## 🎯 Описание

**CustomJoinMessage** — профессиональный плагин для кастомизации сообщений при входе и выходе игроков на сервере Minecraft. Плагин предоставляет современные, визуально привлекательные сообщения с поддержкой анимаций, градиентов, кликабельных ссылок и служебных уведомлений для администраторов.

Создан для серверов на **Paper 1.21.11** с использованием современных технологий: **MiniMessage**, **Adventure API** и **PlaceholderAPI**.

---

## 🌟 Ключевые особенности

### 🎨 Для игроков

| Фича | Описание |
|------|----------|
| **Градиенты и эффекты** | Красивые сообщения с плавными переходами цветов через MiniMessage |
| **Анимированные сообщения** | Строки появляются поочерёдно с настраиваемой задержкой |
| **Кликабельные ссылки** | Встроенные кнопки для перехода в Discord, Telegram и другие ресурсы |
| **Разные сообщения** | Отдельные приветствия для новых и старых игроков |
| **Плейсхолдеры** | `{player}`, `{online}`, `{max}`, `{discord_url}` + полная поддержка PlaceholderAPI |
| **Персональная доставка** | Сообщения видит только сам игрок (опционально: трансляция всем) |

### 👮 Для администраторов

| Фича | Описание |
|------|----------|
| **Служебные уведомления** | Уведомления в чате о входе/выходе игроков — только для админов |
| **Гибкое управление** | Каждый тип сообщения можно включить/отключить независимо |
| **Два формата** | Поддержка как старых `&`-кодов, так и современного MiniMessage |
| **Фильтрация** | Показ только для OP или игроков с пермишеном |

### 🌐 Локализация

| Фича | Описание |
|------|----------|
| **Два языка** | Русский и английский (выбор в конфиге) |
| **Гибкая система** | Все тексты вынесены в отдельные файлы `messages_ru.yml` / `messages_en.yml` |
| **Автоматический фолбэк** | При ошибке загрузки языка используется русский по умолчанию |

---

## 📦 Установка

### Быстрая установка

1. **Скачайте последнюю версию** с [Releases](https://github.com/Nixend-creator/CustomJoinMessage/releases)

2. **Поместите файл** `CustomJoinMessage-1.3.0.jar` в папку `plugins/` вашего сервера

3. **Перезапустите сервер**

4. **Настройте конфигурацию** в `plugins/CustomJoinMessage/config.yml`

### Требования

| Компонент | Версия | Обязательно |
|-----------|--------|-------------|
| **Сервер** | Paper 1.21.11 | ✅ Да |
| **Java** | 21+ | ✅ Да |
| **PlaceholderAPI** | 2.11.6+ | ❌ Нет (опционально) |

> ⚠️ **Важно!** Плагин **не работает** на Spigot/Vanilla из-за зависимости от Paper Adventure API.

---

## ⚙️ Конфигурация

### Быстрый старт (`config.yml`)

```yaml
# ========================================
# CustomJoinMessage - Configuration v1.3.0
# ========================================

# Language: ru (Russian) or en (English)
language: ru

# Server name (used in {server} placeholder)
server-name: "NexusCraft"

# Social links
discord-url: "https://discord.gg/example"
telegram-url: "https://t.me/example"

# Animation settings
use-animation: true
animation-delay-ticks: 5

# Broadcast messages to all players (instead of personal delivery)
broadcast-to-server: false

# --- Player messages ---
welcome-message-new:
  - "<gradient:#00FFAA:#00AA55>Welcome to <gold>{server}</gold>, <bold><yellow>{player}</yellow></bold>!</gradient>"
  - "<gray>Check out our communities:</gray>"
  - "<gold><click:open_url:'{discord_url}'><hover:show_text:'<gold>Click to join Discord'><u>Discord Server</u></hover></click></gold> | <aqua><click:open_url:'{telegram_url}'><hover:show_text:'<aqua>Click to join Telegram'><u>Telegram Channel</u></hover></click></aqua>"

welcome-message-existing:
  - "<gradient:#55AAFF:#0055FF>Welcome back, <bold><aqua>{player}</aqua></bold>!</gradient>"
  - "<gray>Players online: <gold>{online}/{max}</gold> on <gold>{server}</gold></gray>"

quit-message:
  - "<gray><italic>{player}</italic> left <gold>{server}</gold></gray>"

# --- Admin chat notifications ---
EnableFirstJoinMessage: true
EnableJoinMessage: true
EnableLeaveMessage: true

FirstJoinMessage: '&a&l%player%&r &7joined the server for the first time! &e✨'
JoinMessage: '&a+ &f%player%&r &7joined the server.'
LeaveMessage: '&c- &f%player%&r &7left the server.'

# Show notifications only to OP (true) or also to players with permission (false)
admin-only-mode: true
admin-permission: "customjoinmessage.see"