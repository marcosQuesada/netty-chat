package com.marcosquesada.netty.chat.server.chat;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

public class ChatTest {

    @Test
    public void SubscribeToTopicIncludesUserAsSubscriber(){
        InMemoryChat chat = new InMemoryChat(10, 10);
        String topic = "fakeTopic";
        chat.subscribe(topic, "foo");
        chat.subscribe(topic, "bar");

        Collection<String> subscribers =  chat.getSubscribers(topic);

        Assert.assertEquals(2, subscribers.size());
        Assert.assertTrue(subscribers.contains("foo"));
        Assert.assertTrue(subscribers.contains("bar"));
    }

    @Test
    public void UnSubscribeToTopicRemovesUserAsSubscriber(){
        InMemoryChat chat = new InMemoryChat(10, 10);
        String topic = "fakeTopic";
        chat.subscribe(topic, "foo");

        Collection<String> subscribers =  chat.getSubscribers(topic);
        Assert.assertEquals(1, subscribers.size());
        chat.unsubscribe(topic, "foo");

        subscribers =  chat.getSubscribers(topic);

        Assert.assertFalse(subscribers.contains("foo"));
        Assert.assertEquals(0, subscribers.size());
    }

    @Test
    public void OnUserAdditionIfAlreadyExistsTrowException(){
        InMemoryChat chat = new InMemoryChat(10, 10);
        String topic = "fakeTopic";
        chat.subscribe(topic, "foo");

        try {
            chat.subscribe(topic, "foo");
            Assert.fail();
        }catch (Exception e){
            Assert.assertTrue(e.getMessage().equals(Room.USER_ALREADY_JOINED));
        }
    }

    @Test
    public void RoomHistoryEntryListGetsCappedOnMaxEntriesAchieved(){
        int maxHistorySize = 3;
        InMemoryChat chat = new InMemoryChat(10, maxHistorySize);
        String topic = "fakeTopic";
        chat.addToHistory(topic, "msg1");
        chat.addToHistory(topic, "msg2");
        chat.addToHistory(topic, "msg3");

        List<Entry> entries = chat.getHistory(topic);
        Assert.assertEquals(3, entries.size());

        chat.addToHistory(topic, "msg4");

        entries = chat.getHistory(topic);
        Assert.assertEquals(maxHistorySize, entries.size());

        Entry entry = entries.get(0);
        Assert.assertTrue(entry.getMessage().equals("msg2"));
    }
}
