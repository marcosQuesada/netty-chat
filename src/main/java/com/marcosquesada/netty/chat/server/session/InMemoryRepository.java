package com.marcosquesada.netty.chat.server.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRepository implements SessionRepository{
    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    public InMemoryRepository(){}

    public Session get(String userName) {
        return sessions.get(userName);
    }

    public void add(Session session) {
        sessions.put(session.getUserName(), session);
    }

    public void remove(String userName) {
        sessions.remove(userName);
    }

    public synchronized Boolean contains(String userName) {
        return sessions.containsKey(userName);
    }

    public static Session buildSession(ChannelHandlerContext ctx, String userName) {
        return new Session(ctx, userName);
    }
}
