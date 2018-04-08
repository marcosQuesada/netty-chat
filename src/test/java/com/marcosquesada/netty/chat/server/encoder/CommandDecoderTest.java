package com.marcosquesada.netty.chat.server.encoder;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.server.command.ChatCommand;
import com.marcosquesada.netty.chat.server.handler.AuthHandler;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.CharBuffer;

public class CommandDecoderTest {

    @Test
    public void decodeRawMessageToCommand(){

        EmbeddedChannel channel = new EmbeddedChannel( new StringEncoder(), new CommandDecoder(CharsetUtil.UTF_8) );

        channel.writeInbound( ByteBufUtil.encodeString(new PooledByteBufAllocator(), CharBuffer.wrap("/login foo bar" ), CharsetUtil.UTF_8));

        CommandRequest cmd = channel.readInbound();

        Assert.assertNotNull(cmd);
        Assert.assertTrue(cmd.getCmd().equals(AuthHandler.LOGIN_COMMAND));
        Assert.assertEquals(2, cmd.getArguments().length);
        Assert.assertTrue(cmd.getArguments()[0].equals("foo"));
        Assert.assertTrue(cmd.getArguments()[1].equals("bar"));
    }

    @Test
    public void decodeRawMessageToPublishRequest(){

        EmbeddedChannel channel = new EmbeddedChannel( new StringEncoder(), new CommandDecoder(CharsetUtil.UTF_8) );

        channel.writeInbound( ByteBufUtil.encodeString(new PooledByteBufAllocator(), CharBuffer.wrap("Hi there!!" ), CharsetUtil.UTF_8));

        CommandRequest cmd = channel.readInbound();

        Assert.assertNotNull(cmd);
        Assert.assertTrue(cmd.getCmd().equals(ChatCommand.PUBLISH_COMMAND));
        Assert.assertEquals(1, cmd.getArguments().length);
        Assert.assertTrue(cmd.getArguments()[0].equals("Hi there!!"));
    }
}
