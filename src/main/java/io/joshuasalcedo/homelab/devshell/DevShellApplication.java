package io.joshuasalcedo.homelab.devshell;

import io.joshuasalcedo.commonlibs.text.BannerGenerator;
import io.joshuasalcedo.commonlibs.text.TextUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class DevShellApplication {

    public static void main(String[] args) {
        // Show banner immediately at startup
        PrintStream printStream = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        // Get version from the manifest or build properties
        String version = DevShellApplication.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "unknown";
        }

        printStream.println(BannerGenerator.create("DEV SHELL")
                .titleColor(TextUtility.Color.BRIGHT_MAGENTA)
                .showTimestamp()
                .addMetadata("version", version)
                .generateWithAsciiArt());

        // Configure for minimal startup logging
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("logging.level.root", "ERROR");
        System.setProperty("logging.level.org.springframework", "ERROR");

        SpringApplication app = new SpringApplication(DevShellApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }
}