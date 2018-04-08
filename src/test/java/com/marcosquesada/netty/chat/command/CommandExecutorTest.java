package com.marcosquesada.netty.chat.command;

import com.marcosquesada.netty.chat.server.session.Session;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CommandExecutorTest {

    private static String FOO_COMMAND = "foo";

    @Test
    public void commandExecutorRegisterAndExecuteFakeCommand() {
        CommandExecutor executor = new CommandExecutor();

        executor.register(new FakeCommandHandler());

        Assert.assertTrue(executor.contains(FOO_COMMAND));

        Session session = mock(Session.class);
        String[] args = {"arg"};
        CommandRequest request = new CommandRequest(FOO_COMMAND, args);

        executor.execute(session, request);

        verify(session, times(1)).send("received arg");
    }

    class FakeCommandHandler implements CommandHandler{

        public Map<String, BiConsumer<Session, String[]>> handlers() throws RuntimeException {
            Map<String, BiConsumer<Session, String[]>> handlers = new HashMap<>();
            handlers.put(FOO_COMMAND, this::foo);

            return handlers;
        }

        private void foo(Session session, String[] arguments) {

            session.send(String.format("received %s", arguments[0]));
        }

    }
}
