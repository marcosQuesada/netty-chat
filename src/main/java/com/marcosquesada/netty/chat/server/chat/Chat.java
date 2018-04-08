package com.marcosquesada.netty.chat.server.chat;

import java.util.Collection;
import java.util.List;

public interface Chat {

    void subscribe(String topic, String userName);

    void unsubscribe(String topic, String userName);

    Collection<String> getSubscribers(String topic);

    void addToHistory(String topic, String msg);

    List<Entry> getHistory(String topic);
}
