package com.marcosquesada.netty.chat.server;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.command.Commander;
import com.marcosquesada.netty.chat.executor.Executor;
import com.marcosquesada.netty.chat.server.chat.Chat;
import com.marcosquesada.netty.chat.server.command.ChatCommand;
import com.marcosquesada.netty.chat.server.session.Session;
import com.marcosquesada.netty.chat.server.session.SessionRepository;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RouterTest {

    @Test
    public void routerAcceptsSessionIsRegistered(){
        Chat chat = mock(Chat.class);
        Commander cmdExec = mock(Commander.class);
        Executor exec = mock(Executor.class);

        SessionRepository repository = mock(SessionRepository.class);
        when(repository.contains(anyString())).thenReturn(true);

        Router router = new Router(chat, cmdExec, exec, repository);

        Session session = mock(Session.class);
        when(session.getUserName()).thenReturn("foo");

        router.accept(session);

        verify(repository, times(1)).add(session);
    }

    @Test
    public void routerAcceptsAndCloseSessionRegisterAndRemoveSession(){
        Chat chat = mock(Chat.class);
        Commander cmdExec = mock(Commander.class);
        Executor exec = mock(Executor.class);

        Session session = mock(Session.class);
        when(session.getUserName()).thenReturn("foo");
        when(session.getCurrentTopic()).thenReturn("topic");
        when(session.hasTopic()).thenReturn(true);

        SessionRepository repository = mock(SessionRepository.class);
        when(repository.contains(anyString())).thenReturn(true);
        when(repository.get(anyString())).thenReturn(session);

        Router router = new Router(chat, cmdExec, exec, repository);

        router.accept(session);

        router.close("foo");

        verify(repository, times(1)).remove("foo");
        verify(chat, times(1)).unsubscribe("topic", "foo");
    }

    @Test
    public void routerReceiveMessageAndForwardsToCommandExecutor() {
        Chat chat = mock(Chat.class);
        Commander cmdExec = mock(Commander.class);
        when(cmdExec.contains(anyString())).thenReturn(true);

        Executor exec = mock(Executor.class);

        Session session = mock(Session.class);
        when(session.getUserName()).thenReturn("foo");
        when(session.getCurrentTopic()).thenReturn("topic");
        when(session.hasTopic()).thenReturn(true);

        SessionRepository repository = mock(SessionRepository.class);
        when(repository.contains(anyString())).thenReturn(true);
        when(repository.get(anyString())).thenReturn(session);

        Router router = new Router(chat, cmdExec, exec, repository);

        router.accept(session);

        String[] args = {"topic"};
        CommandRequest request = new CommandRequest(ChatCommand.JOIN_COMMAND, args);
        router.receiveMessage("foo", request);

        verify(exec, times(1)).execute(any());

    }
}
