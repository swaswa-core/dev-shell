# Dev Shell v1.0.0

**Release Date:** July 22, 2025

## What's New

### Smart Commit
- Automatically creates temporary branch, stages files, and merges back
- Adds list of changed files to commit message
- Optional push with `--push` flag

### Commands
- **Git**: `commit`, `status`, `add`, `log`, `config`, `auth`, `git-init`
- **Navigation**: `pwd`, `cd`, `ls`
- **System**: Any unrecognized command runs as system command
- **Utility**: `set-alias` to create shell aliases

### Features
- Clean startup with ASCII banner
- Colored output with emojis
- Production-ready logging
- Pass-through system command execution

### Requirements
- Java 24+
- Maven 3.8+

### Installation
```bash
git clone git@github.com:swaswa-core/dev-shell.git
cd dev-shell
./mvnw clean package
java -jar target/dev-shell.jar
```

---
Created by Joshua Salcedo