#!/bin/bash
# First, add the CliLogger import to files that use loggers (if not already present)
find . -name "*.java" -type f -exec grep -l -E "(log\.|logger\.|Log\.|Logger\.|LOGGER\.|LOG\.)(info|warn|error|debug)" {} \; | while read file; do
    if ! grep -q "import io.joshuasalcedo.homelab.devshell.utils.CliLogger;" "$file"; then
        sed -i '/^package/a\\nimport io.joshuasalcedo.homelab.devshell.utils.CliLogger;' "$file"
    fi
done

# Remove old logger imports and declarations
find . -name "*.java" -type f -exec sed -i \
    -e '/import org\.slf4j\.Logger;/d' \
    -e '/import org\.slf4j\.LoggerFactory;/d' \
    -e '/import io\.joshuasalcedo\.commonlibs\.logging\.api\.Logger;/d' \
    -e '/private static final Logger logger = LoggerFactory\.getLogger/d' \
    -e '/private final Logger logger = LoggerFactory\.getLogger/d' {} \;

# Replace logger calls with CliLogger calls
find . -name "*.java" -type f -exec sed -i \
    -e 's/logger\.info(/CliLogger.info(/g' \
    -e 's/logger\.warn(/CliLogger.warn(/g' \
    -e 's/logger\.error(/CliLogger.error(/g' \
    -e 's/logger\.debug(/CliLogger.debug(/g' \
    -e 's/Logger\.info(/CliLogger.info(/g' \
    -e 's/Logger\.warn(/CliLogger.warn(/g' \
    -e 's/Logger\.error(/CliLogger.error(/g' \
    -e 's/Logger\.debug(/CliLogger.debug(/g' {} \;
