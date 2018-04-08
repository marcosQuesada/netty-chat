package com.marcosquesada.netty.chat.command;

public class CommandRequest {
    private String cmd;
    private String[] arguments;

    public CommandRequest(String cmd, String[] arguments) {
        this.cmd = cmd;
        this.arguments = arguments;
    }

    public String getCmd() {
        return cmd;
    }

    public String[] getArguments() {
        return arguments;
    }
}
