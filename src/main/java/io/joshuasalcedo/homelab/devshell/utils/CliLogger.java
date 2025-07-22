package io.joshuasalcedo.homelab.devshell.utils;

import io.joshuasalcedo.commonlibs.text.TextUtility;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * CliLogger class with placeholder support.
 *
 * @author JoshuaSalcedo
 * @created 7/22/2025 10:01 PM
 * @since ${PROJECT.version}
 */
public class CliLogger {
    private static final PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    
    private CliLogger(){
        throw new IllegalStateException("Utility class");
    }

    public static void warn(String message, Object... args){
        String formattedMessage = formatMessage(message, args);
        println(formattedMessage, TextUtility.Color.YELLOW);
    }

    public static void info(String message, Object... args){
        String formattedMessage = formatMessage(message, args);
        println(formattedMessage, TextUtility.Color.BLUE);
    }

    public static void error(String message, Object... args){
        String formattedMessage = formatMessage(message, args);
        println(formattedMessage, TextUtility.Color.RED);
    }

    public static void debug(String message, Object... args){
        String formattedMessage = formatMessage(message, args);
        println(formattedMessage, TextUtility.Color.GRAY);
    }

    private static void println(String message, TextUtility.Color color){
        out.println(TextUtility.of(message)
                .bold()
                .color(color));
    }
    
    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        
        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int i = 0;
        
        while (i < message.length()) {
            if (i < message.length() - 1 && message.charAt(i) == '{' && message.charAt(i + 1) == '}') {
                if (argIndex < args.length) {
                    result.append(args[argIndex] != null ? args[argIndex].toString() : "null");
                    argIndex++;
                } else {
                    result.append("{}");
                }
                i += 2;
            } else {
                result.append(message.charAt(i));
                i++;
            }
        }
        
        return result.toString();
    }
}