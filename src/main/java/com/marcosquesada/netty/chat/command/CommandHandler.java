package com.marcosquesada.netty.chat.command;

import com.marcosquesada.netty.chat.server.session.Session;

import java.util.Map;
import java.util.function.BiConsumer;

public interface CommandHandler {

    // Command Handler definitions
    Map<String, BiConsumer<Session, String[]>> handlers() throws RuntimeException;

}

