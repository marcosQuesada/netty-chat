package com.marcosquesada.netty.chat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import com.marcosquesada.netty.chat.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.System.exit;

public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        configureLogger();

        Server server = new Server(9999);

        logger.info("Starting Server");
        server.start();

        waitUntilKeypressed();

        logger.info("Closing Server");
        server.terminate();

        exit(0);
    }

    private static void waitUntilKeypressed() {
        try {
            System.in.read();
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void configureLogger(){
        try {
            LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();

            PatternLayoutEncoder ple = new PatternLayoutEncoder();
            ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
            ple.setContext(context);
            ple.start();

            ch.qos.logback.classic.Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.INFO);

            ch.qos.logback.classic.Logger logger = context.getLogger("com.marcosquesada.netty.chat");
            logger.setLevel(Level.DEBUG);

        } catch (Exception je) {
            logger.error("Unexpected exception configuring log4j, err {}", je.getMessage());
        }
    }
}
