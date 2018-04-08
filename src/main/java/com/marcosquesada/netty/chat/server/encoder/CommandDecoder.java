package com.marcosquesada.netty.chat.server.encoder;

import com.marcosquesada.netty.chat.command.CommandRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

public class CommandDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final Charset charset;

    public CommandDecoder(Charset charset) {
        this.charset = charset;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String rawMsg = msg.toString(this.charset);
        if (rawMsg.equals("")) {
            return;
        }

        //On Command request
        if (rawMsg.substring(0, 1).equals("/")) {
            out.add(buildCommandRequest(rawMsg));

            return;
        }

        // On Publish
        out.add(buildPublishRequest(rawMsg));
    }

    private CommandRequest buildCommandRequest(String rawMsg) {
        String cmd = rawMsg.substring(1, rawMsg.length());
        String[] cmdParts = cmd.split(" ");
        Integer size = cmdParts.length - 1;
        String[] args = new String[(size < 0) ? 0 : size];
        System.arraycopy(cmdParts, 1, args, 0, cmdParts.length - 1);

        return new CommandRequest(cmdParts[0], args);
    }

    private CommandRequest buildPublishRequest(String rawMsg) {
        String[] args = new String[1];
        args[0] = rawMsg;

        return new CommandRequest("publish", args);
    }
}