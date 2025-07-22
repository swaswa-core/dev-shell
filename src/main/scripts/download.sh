#!/bin/bash
#
# Dev-Shell Installation Script
# This script downloads dev-shell.jar and creates a 'dev' command
#

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DOWNLOAD_URL="https://github.com/swaswa-core/dev-shell/releases/download/v1.0.1/dev-shell.jar"
INSTALL_DIR="$HOME/.dev-shell"
JAR_FILE="$INSTALL_DIR/dev-shell.jar"
BIN_DIR="$HOME/.local/bin"
COMMAND_NAME="dev"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Java is installed
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or higher."
        exit 1
    fi

    # Check Java version
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        print_warning "Java version is less than 17. Dev-shell may not work properly."
    fi
}

# Create installation directory
create_directories() {
    print_status "Creating installation directory: $INSTALL_DIR"
    mkdir -p "$INSTALL_DIR"

    print_status "Creating bin directory: $BIN_DIR"
    mkdir -p "$BIN_DIR"
}

# Download dev-shell.jar
download_jar() {
    print_status "Downloading dev-shell.jar from GitHub..."

    if command -v curl &> /dev/null; then
        curl -L -o "$JAR_FILE" "$DOWNLOAD_URL" || {
            print_error "Failed to download dev-shell.jar"
            exit 1
        }
    elif command -v wget &> /dev/null; then
        wget -O "$JAR_FILE" "$DOWNLOAD_URL" || {
            print_error "Failed to download dev-shell.jar"
            exit 1
        }
    else
        print_error "Neither curl nor wget is installed. Please install one of them."
        exit 1
    fi

    print_status "Downloaded successfully to: $JAR_FILE"
}

# Create the 'dev' command script
create_command() {
    local script_path="$BIN_DIR/$COMMAND_NAME"

    print_status "Creating '$COMMAND_NAME' command..."

    cat > "$script_path" << 'EOF'
#!/bin/bash
#
# Dev-Shell launcher
#

# Configuration
JAR_FILE="$HOME/.dev-shell/dev-shell.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: dev-shell.jar not found at $JAR_FILE"
    echo "Please run the install script again."
    exit 1
fi

# JVM options (can be customized via environment variable)
JVM_OPTS="${DEV_SHELL_JVM_OPTS:--Xms256m -Xmx512m}"

# Launch dev-shell
java $JVM_OPTS -jar "$JAR_FILE" "$@"
EOF

    chmod +x "$script_path"
    print_status "Created command script at: $script_path"
}

# Update shell configuration
update_shell_config() {
    local shell_config=""
    local shell_name=$(basename "$SHELL")

    case "$shell_name" in
        bash)
            shell_config="$HOME/.bashrc"
            ;;
        zsh)
            shell_config="$HOME/.zshrc"
            ;;
        *)
            shell_config="$HOME/.profile"
            ;;
    esac

    # Remove existing 'dev' alias if it exists
    if grep -q "alias dev=" "$shell_config" 2>/dev/null; then
        print_status "Removing existing 'dev' alias from $shell_config"
        sed -i '/^alias dev=/d' "$shell_config"
    fi

    # Check if PATH already contains the bin directory
    if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
        print_status "Adding $BIN_DIR to PATH in $shell_config"
        echo "" >> "$shell_config"
        echo "# Added by dev-shell installer" >> "$shell_config"
        echo "export PATH=\"\$HOME/.local/bin:\$PATH\"" >> "$shell_config"
        print_warning "Please run 'source $shell_config' or restart your terminal"
    else
        print_status "PATH already contains $BIN_DIR"
    fi
}

# Verify installation
verify_installation() {
    if [ -f "$JAR_FILE" ] && [ -f "$BIN_DIR/$COMMAND_NAME" ]; then
        print_status "Installation completed successfully!"
        echo ""
        echo "Dev-shell has been installed to: $INSTALL_DIR"
        echo "Command '$COMMAND_NAME' has been created in: $BIN_DIR"
        echo ""

        if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
            echo "To use the '$COMMAND_NAME' command, run:"
            echo "  source ~/.$(basename $SHELL)rc"
            echo ""
            echo "Or add this to your PATH manually:"
            echo "  export PATH=\"\$HOME/.local/bin:\$PATH\""
        else
            echo "You can now use the '$COMMAND_NAME' command!"
        fi

        echo ""
        echo "To customize JVM options, set the DEV_SHELL_JVM_OPTS environment variable:"
        echo "  export DEV_SHELL_JVM_OPTS=\"-Xms512m -Xmx1g\""
    else
        print_error "Installation verification failed!"
        exit 1
    fi
}

# Main installation process
main() {
    echo "======================================"
    echo "Dev-Shell Installation Script"
    echo "======================================"
    echo ""

    check_java
    create_directories
    download_jar
    create_command
    update_shell_config
    verify_installation
}

# Run the installer
main "$@"