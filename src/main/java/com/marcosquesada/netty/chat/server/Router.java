package com.marcosquesada.netty.chat.server;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.command.Commander;
import com.marcosquesada.netty.chat.executor.Executor;
import com.marcosquesada.netty.chat.server.chat.Chat;
import com.marcosquesada.netty.chat.server.session.Session;
import com.marcosquesada.netty.chat.server.session.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Router{
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static String COMMAND_NOT_FOUND = "Command not found!";
    public static String UNEXPECTED_ERROR = "Execution Error";

    private Chat broker;
    private SessionRepository sessions;
    private Executor executor;
    private Commander commandExecutor;

    public Router(Chat broker, Commander cmdExec, Executor executor, SessionRepository sessions) {
        this.broker = broker;
        this.commandExecutor = cmdExec;
        this.executor = executor;
        this.sessions = sessions;
    }

    public void accept(Session session) {
        logger.info("Router accept ", session.getUserName());
        sessions.add(session);
    }

    public void close(String userName) {
        Session session = sessions.get(userName);
        if (session == null) {
            logger.error("Session not found on close ", userName);
            return;
        }

        sessions.remove(userName);
        if (session.hasTopic()) {
            broker.unsubscribe(session.getCurrentTopic(), userName);
        }
    }

    public void receiveMessage(String userName, CommandRequest cmd) {
        Session session = sessions.get(userName);
        if (session == null) {
            logger.error("Session not found on userName %s", userName);
            return;
        }

        if (!commandExecutor.contains(cmd.getCmd())) {
            session.send(COMMAND_NOT_FOUND);
            return;
        }

        // Delegate command execution to its own thread pool
        executor.execute(() -> {
            logger.info(String.format("User %s Received CMD: %s", userName, cmd.getCmd()));
            try {
                commandExecutor.execute(session, cmd);
            } catch (Exception e) {
                logger.error("Unexpected exception executing task ", e.getMessage());
                session.send(UNEXPECTED_ERROR);
                e.printStackTrace();
            }
        });
    }
}
