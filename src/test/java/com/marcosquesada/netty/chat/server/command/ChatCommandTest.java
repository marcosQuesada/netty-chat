package com.marcosquesada.netty.chat.server.command;

import com.marcosquesada.netty.chat.server.chat.Chat;
import com.marcosquesada.netty.chat.server.chat.Entry;
import com.marcosquesada.netty.chat.server.session.Session;
import com.marcosquesada.netty.chat.server.session.SessionRepository;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ChatCommandTest {

    @Test
    public void OnChatUserJoinedUserGetsSubscribedToRoomTopic() {
        Chat chat = mock(Chat.class);
        SessionRepository repo = mock(SessionRepository.class);
        ChatCommand cmd = new ChatCommand(chat, repo);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        Session session = new Session(ctx, "fooUser");

        Map<String, BiConsumer<Session, String[]>> handlers = cmd.handlers();
        String[] args = {"topic"};
        handlers.get(ChatCommand.JOIN_COMMAND).accept(session, args);

        verify(chat, times(1)).subscribe(anyString(), anyString());
        verify(ctx, times(1)).writeAndFlush(anyString());
        Assert.assertTrue(session.getCurrentTopic().equals("topic"));
    }

    @Test
    public void OnChatUserLeaveUserGetsUnSubscribedAndDisconnects() {
        Chat chat = mock(Chat.class);
        SessionRepository repo = mock(SessionRepository.class);
        ChatCommand cmd = new ChatCommand(chat, repo);

        Session session = mock(Session.class);
        when(session.hasTopic()).thenReturn(true);

        Map<String, BiConsumer<Session, String[]>> handlers = cmd.handlers();
        String[] args = {"topic"};
        handlers.get(ChatCommand.JOIN_COMMAND).accept(session, args);

        String[] leaveArgs = {};
        handlers.get(ChatCommand.LEAVE_COMMAND).accept(session, leaveArgs);

        verify(chat, times(1)).unsubscribe(anyString(), anyString());
        verify(session, times(2)).send(anyString()); //Joined and Leaved
        verify(session, times(1)).terminate();
    }

    @Test
    public void UsersCommandReturnsRoomTopicUsers() {
        Chat chat = mock(Chat.class);
        Collection<String> users = new ArrayList<>();
        users.add("foo");

        when(chat.getSubscribers(anyString())).thenReturn(users);

        SessionRepository repo = mock(SessionRepository.class);
        ChatCommand cmd = new ChatCommand(chat, repo);

        Session session = mock(Session.class);
        when(session.hasTopic()).thenReturn(true);
        when(session.getUserName()).thenReturn("foo");
        when(session.getCurrentTopic()).thenReturn("topic");

        Map<String, BiConsumer<Session, String[]>> handlers = cmd.handlers();
        String[] args = {"topic"};
        handlers.get(ChatCommand.JOIN_COMMAND).accept(session, args);
        verify(session, times(1)).send("Joined room topic");

        handlers.get(ChatCommand.USERS_COMMAND).accept(session, args);

        verify(session, times(1)).send("Users on topic topic");
        verify(session, times(1)).send(" -foo");
    }

    @Test
    public void OnPublishCommandSendMessageToAllRoomTopicUsers() {
        Chat chat = mock(Chat.class);
        Collection<String> users = new ArrayList<>();
        users.add("foo");
        users.add("bar");
        when(chat.getSubscribers(anyString())).thenReturn(users);

        Session session = mock(Session.class);
        when(session.hasTopic()).thenReturn(true);
        when(session.getUserName()).thenReturn("foo");
        when(session.getCurrentTopic()).thenReturn("topic");

        Session sessionB = mock(Session.class);
        when(sessionB.hasTopic()).thenReturn(true);
        when(sessionB.getUserName()).thenReturn("bar");
        when(sessionB.getCurrentTopic()).thenReturn("topic");

        SessionRepository repo = mock(SessionRepository.class);
        when(repo.get("foo")).thenReturn(session);
        when(repo.get("bar")).thenReturn(sessionB);

        ChatCommand cmd = new ChatCommand(chat, repo);

        Map<String, BiConsumer<Session, String[]>> handlers = cmd.handlers();
        String[] args = {"topic"};
        handlers.get(ChatCommand.JOIN_COMMAND).accept(session, args);
        verify(session, times(1)).send("Joined room topic");

        String[] pubArgs = {"hi"};
        handlers.get(ChatCommand.PUBLISH_COMMAND).accept(session, pubArgs);

        verify(sessionB, times(1)).send("hi");
    }

}

