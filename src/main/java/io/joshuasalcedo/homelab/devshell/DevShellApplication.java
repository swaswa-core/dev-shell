package io.joshuasalcedo.homelab.devshell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevShellApplication {

    public static void main(String[] args) {
        // Configure for minimal startup logging
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("logging.level.root", "ERROR");
        System.setProperty("logging.level.org.springframework", "ERROR");
        
        SpringApplication app = new SpringApplication(DevShellApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }

}
