package io.joshuasalcedo.homelab.devshell.utils;

import io.joshuasalcedo.homelab.devshell.utils.CliLogger;

import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class AliasSetter {
    
    /**
     * Finds the JAR file of the currently running program or looks for it in the target directory.
     * 
     * @return The File object representing the JAR file, or null if not found
     */
    private static File findJar() {
        try {
            // Get the location of this class
            URL location = AliasSetter.class.getProtectionDomain().getCodeSource().getLocation();
            String locationPath = location.getPath();
            
            // Handle different URI schemes
            if (location.getProtocol().equals("file")) {
                File file = new File(location.toURI());
                
                // Check if it's actually a JAR file
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    return file;
                }
                
                // If we're in development (classes directory), look for JAR in target
                if (file.isDirectory() && file.getPath().contains("target/classes")) {
                    File targetDir = file.getParentFile();
                    File[] jarFiles = targetDir.listFiles((dir, name) -> 
                        name.endsWith(".jar") && !name.contains("sources") && !name.contains("javadoc"));
                    
                    if (jarFiles != null && jarFiles.length > 0) {
                        return jarFiles[0]; // Return the first JAR found
                    }
                }
            }
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            CliLogger.error("Cannot Find the JAR file: " + e.getMessage());
        }
        return null;
    }

    public static String printAliasCommand(String aliasName) {
        File jar = findJar();
        if (jar == null) {
            // Fallback: provide generic instructions
            String currentDir = System.getProperty("user.dir");
            String jarPath = currentDir + "/target/dev-shell.jar";
            
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return String.format("@echo off\njava -jar \"%s\" %%*", jarPath);
            } else {
                return String.format("alias %s='java -jar \"%s\"'", aliasName, jarPath);
            }
        }

        String jarPath = jar.getAbsolutePath();

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // Return Windows batch file content
            return String.format("@echo off\njava -jar \"%s\" %%*", jarPath);
        } else {
            // Return Unix/Linux/Mac alias command - use double quotes to avoid escaping issues
            return String.format("alias %s=\"java -jar '%s'\"", aliasName, jarPath);
        }
    }

}