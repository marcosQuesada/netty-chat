package com.marcosquesada.netty.chat.server.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChat implements Chat {
    public static Integer MAX_USERS_BY_ROOM = 10;
    public static Integer HISTORY_SIZE = 10;

    private volatile Map<String, Room> subscribers = new ConcurrentHashMap<>();
    private Integer maxUsersByRoom;
    private Integer historySize;

    public InMemoryChat(Integer maxUsersByRoom, Integer historySize) {
        this.maxUsersByRoom = maxUsersByRoom;
        this.historySize = historySize;
    }

    public synchronized void subscribe(String topic, String userName) {

        if (!subscribers.containsKey(topic)) {
            subscribers.put(topic, new Room(maxUsersByRoom, historySize));
        }

        Room room = subscribers.get(topic);
        if (room.hasUser(userName)) {
            throw new RuntimeException(Room.USER_ALREADY_JOINED);
        }

        room.addUser(userName);
    }

    public synchronized void unsubscribe(String topic, String userName) {
        Room room = subscribers.get(topic);
        if (room == null) {
            return;
        }

        room.removeUser(userName);
    }

    public synchronized Collection<String> getSubscribers(String topic) {
        if (!subscribers.containsKey(topic)) {
            return new ArrayList<>();
        }

        return subscribers.get(topic).subscribers();
    }

    public void addToHistory(String topic, String msg) {
        Room room = subscribers.get(topic);
        if (room == null) {
            room = new Room(maxUsersByRoom, historySize);
            subscribers.put(topic, room);
        }

        room.addToHistory(msg);
    }

    public List<Entry> getHistory(String topic) {
        Room room = subscribers.get(topic);
        if (room == null) {
            return new ArrayList<>();
        }

        return room.getHistory();
    }

}
