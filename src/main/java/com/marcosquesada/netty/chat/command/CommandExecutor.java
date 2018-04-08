package com.marcosquesada.netty.chat.command;

import com.marcosquesada.netty.chat.server.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CommandExecutor implements Commander{
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private Map<String, BiConsumer<Session, String[]>> commands= new HashMap<>();

    public CommandExecutor() {
    }

    public void register(CommandHandler commandHandler) {
        for (Map.Entry<String, BiConsumer<Session, String[]>> cmd : commandHandler.handlers().entrySet()) {
            commands.put(cmd.getKey(), cmd.getValue());
        }
    }

    public void execute(Session session, CommandRequest cmd) {
        if (!commands.containsKey(cmd.getCmd())) {
            logger.error(String.format("Command %s Not found", cmd.getCmd()));
            return;
        }

        commands.get(cmd.getCmd()).accept(session, cmd.getArguments());
    }

    public boolean contains(String cmdType) {
        return commands.containsKey(cmdType);
    }
}
