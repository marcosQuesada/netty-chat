package com.marcosquesada.netty.chat.server.chat;

import java.time.LocalTime;

public class Entry{
    private String message;
    private LocalTime time;

    public Entry(String message) {
        this.message = message;
        this.time = LocalTime.now();
    }

    public LocalTime getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
