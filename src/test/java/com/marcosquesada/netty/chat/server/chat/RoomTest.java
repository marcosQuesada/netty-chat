package com.marcosquesada.netty.chat.server.chat;

import org.junit.Assert;
import org.junit.Test;

import static com.marcosquesada.netty.chat.server.chat.Room.MAX_USERS_ERROR;

public class RoomTest {

    @Test
    public void RoomFullThrowsExceptionOnUserAddition(){
        Room room = new Room(1, 0);

        room.addUser("foo");

        try{
            room.addUser("bar");
            Assert.fail();
        }catch (Exception e) {
            Assert.assertTrue(e.getMessage().equals(MAX_USERS_ERROR));
        }
    }
}
