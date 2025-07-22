#Requires -Version 5.1
<#
.SYNOPSIS
    Dev-Shell Installation Script for Windows
.DESCRIPTION
    This script downloads dev-shell.jar and creates a 'dev' command
.EXAMPLE
    .\install-dev-shell.ps1
#>

# Configuration
$DownloadUrl = "https://github.com/swaswa-core/dev-shell/releases/download/v1.0.1/dev-shell.jar"
$InstallDir = "$env:USERPROFILE\.dev-shell"
$JarFile = "$InstallDir\dev-shell.jar"
$BinDir = "$env:USERPROFILE\.local\bin"
$CommandName = "dev"
$BatchFile = "$BinDir\$CommandName.cmd"
$PowerShellFile = "$BinDir\$CommandName.ps1"

# Colors for output
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] " -ForegroundColor Green -NoNewline
    Write-Host $Message
}

function Write-Error-Message {
    param([string]$Message)
    Write-Host "[ERROR] " -ForegroundColor Red -NoNewline
    Write-Host $Message
}

function Write-Warning-Message {
    param([string]$Message)
    Write-Host "[WARNING] " -ForegroundColor Yellow -NoNewline
    Write-Host $Message
}

# Check if Java is installed
function Test-JavaInstallation {
    Write-Status "Checking Java installation..."

    try {
        $javaVersion = & java -version 2>&1 | Select-String "version" | Select-Object -First 1
        if ($null -eq $javaVersion) {
            Write-Error-Message "Java is not installed. Please install Java 17 or higher."
            Write-Host "Download from: https://adoptium.net/" -ForegroundColor Cyan
            exit 1
        }

        Write-Status "Found Java: $javaVersion"

        # Extract major version
        if ($javaVersion -match '"(\d+)\.') {
            $majorVersion = [int]$Matches[1]
        } elseif ($javaVersion -match '"(\d+)') {
            $majorVersion = [int]$Matches[1]
        }

        if ($majorVersion -lt 17) {
            Write-Warning-Message "Java version is less than 17. Dev-shell may not work properly."
        }
    }
    catch {
        Write-Error-Message "Failed to check Java version: $_"
        exit 1
    }
}

# Create installation directories
function New-InstallationDirectories {
    Write-Status "Creating installation directory: $InstallDir"
    if (!(Test-Path $InstallDir)) {
        New-Item -ItemType Directory -Path $InstallDir -Force | Out-Null
    }

    Write-Status "Creating bin directory: $BinDir"
    if (!(Test-Path $BinDir)) {
        New-Item -ItemType Directory -Path $BinDir -Force | Out-Null
    }
}

# Download dev-shell.jar
function Get-DevShellJar {
    Write-Status "Downloading dev-shell.jar from GitHub..."

    try {
        # Use TLS 1.2 for GitHub
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

        # Download with progress
        $webClient = New-Object System.Net.WebClient
        $webClient.DownloadProgressChanged += {
            Write-Progress -Activity "Downloading dev-shell.jar" -Status "$($_.ProgressPercentage)% Complete" -PercentComplete $_.ProgressPercentage
        }

        $downloadTask = $webClient.DownloadFileTaskAsync($DownloadUrl, $JarFile)

        while (!$downloadTask.IsCompleted) {
            Start-Sleep -Milliseconds 100
        }

        Write-Progress -Activity "Downloading dev-shell.jar" -Completed

        if ($downloadTask.IsFaulted) {
            throw $downloadTask.Exception
        }

        Write-Status "Downloaded successfully to: $JarFile"
    }
    catch {
        Write-Error-Message "Failed to download dev-shell.jar: $_"
        exit 1
    }
}

# Create the 'dev' command files
function New-DevCommand {
    Write-Status "Creating '$CommandName' command files..."

    # Create PowerShell script
    $psContent = @'
<#
.SYNOPSIS
    Dev-Shell launcher
#>

$JarFile = "$env:USERPROFILE\.dev-shell\dev-shell.jar"

# Check if JAR exists
if (!(Test-Path $JarFile)) {
    Write-Host "Error: dev-shell.jar not found at $JarFile" -ForegroundColor Red
    Write-Host "Please run the install script again." -ForegroundColor Red
    exit 1
}

# JVM options (can be customized via environment variable)
$jvmOpts = if ($env:DEV_SHELL_JVM_OPTS) { $env:DEV_SHELL_JVM_OPTS } else { "-Xms256m -Xmx512m" }

# Build command arguments
$javaArgs = @($jvmOpts -split ' ') + @("-jar", $JarFile) + $args

# Launch dev-shell
& java @javaArgs
'@

    Set-Content -Path $PowerShellFile -Value $psContent
    Write-Status "Created PowerShell script at: $PowerShellFile"

    # Create batch file for CMD compatibility
    $batchContent = @"
@echo off
REM Dev-Shell launcher batch file

SET JAR_FILE=%USERPROFILE%\.dev-shell\dev-shell.jar

REM Check if JAR exists
IF NOT EXIST "%JAR_FILE%" (
    echo Error: dev-shell.jar not found at %JAR_FILE%
    echo Please run the install script again.
    exit /b 1
)

REM JVM options (can be customized via environment variable)
IF "%DEV_SHELL_JVM_OPTS%"=="" (
    SET DEV_SHELL_JVM_OPTS=-Xms256m -Xmx512m
)

REM Launch dev-shell
java %DEV_SHELL_JVM_OPTS% -jar "%JAR_FILE%" %*
"@

    Set-Content -Path $BatchFile -Value $batchContent
    Write-Status "Created batch file at: $BatchFile"
}

# Update PATH environment variable
function Update-PathEnvironment {
    Write-Status "Checking PATH environment variable..."

    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")

    if ($currentPath -notlike "*$BinDir*") {
        Write-Status "Adding $BinDir to user PATH..."

        $newPath = if ($currentPath) { "$currentPath;$BinDir" } else { $BinDir }
        [Environment]::SetEnvironmentVariable("Path", $newPath, "User")

        # Update current session
        $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")

        Write-Warning-Message "PATH updated. You may need to restart your terminal for changes to take effect."
    }
    else {
        Write-Status "PATH already contains $BinDir"
    }
}

# Verify installation
function Test-Installation {
    if ((Test-Path $JarFile) -and (Test-Path $BatchFile) -and (Test-Path $PowerShellFile)) {
        Write-Host ""
        Write-Host "======================================"
        Write-Host "Installation completed successfully!" -ForegroundColor Green
        Write-Host "======================================"
        Write-Host ""
        Write-Host "Dev-shell has been installed to: $InstallDir"
        Write-Host "Command '$CommandName' has been created in: $BinDir"
        Write-Host ""
        Write-Host "You can now use the '$CommandName' command from:"
        Write-Host "  - PowerShell"
        Write-Host "  - Command Prompt (CMD)"
        Write-Host "  - Windows Terminal"
        Write-Host ""
        Write-Host "To customize JVM options, set the DEV_SHELL_JVM_OPTS environment variable:"
        Write-Host '  $env:DEV_SHELL_JVM_OPTS = "-Xms512m -Xmx1g"' -ForegroundColor Cyan
        Write-Host ""
        Write-Host "If the command is not found, restart your terminal or run:"
        Write-Host '  $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")' -ForegroundColor Cyan
    }
    else {
        Write-Error-Message "Installation verification failed!"
        exit 1
    }
}

# Main installation process
function Install-DevShell {
    Write-Host "======================================"
    Write-Host "Dev-Shell Installation Script" -ForegroundColor Cyan
    Write-Host "======================================"
    Write-Host ""

    Test-JavaInstallation
    New-InstallationDirectories
    Get-DevShellJar
    New-DevCommand
    Update-PathEnvironment
    Test-Installation
}

# Run the installer
try {
    Install-DevShell
}
catch {
    Write-Error-Message "Installation failed: $_"
    exit 1
}