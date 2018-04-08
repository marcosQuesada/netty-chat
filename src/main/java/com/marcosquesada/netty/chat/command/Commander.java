package com.marcosquesada.netty.chat.command;

import com.marcosquesada.netty.chat.server.session.Session;

public interface Commander {

    void register(CommandHandler commandHandler);

    void execute(Session session, CommandRequest cmd);

    boolean contains(String cmdType);
}
