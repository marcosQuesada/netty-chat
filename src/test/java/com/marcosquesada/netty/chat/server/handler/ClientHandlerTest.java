package com.marcosquesada.netty.chat.server.handler;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.server.Router;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {

    @Test
    public void onClientHandlerAddedRouterAcceptsSession() {
        Router router = mock(Router.class);
        EmbeddedChannel channel = new EmbeddedChannel(new ClientHandler("foo", router));

        String[] args = {"foo", "bar"};
        CommandRequest req = new CommandRequest("login", args);
        channel.writeInbound(req);

        verify(router, times(1)).accept(any());

        String res = channel.readOutbound();

        Assert.assertTrue(res.equals("Welcome foo"));
    }

    @Test
    public void onConnectionClosedClientHandlerDetectsAndRouterCloseSession() {
        Router router = mock(Router.class);
        EmbeddedChannel channel = new EmbeddedChannel(new ClientHandler("foo", router));

        channel.close();

        verify(router, times(1)).close("foo");
    }

}
