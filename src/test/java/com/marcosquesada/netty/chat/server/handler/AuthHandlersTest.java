package com.marcosquesada.netty.chat.server.handler;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.server.Router;
import com.marcosquesada.netty.chat.server.auth.Authenticator;
import com.marcosquesada.netty.chat.server.session.SessionRepository;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AuthHandlersTest {

    @Test
    public void authMiddlewareInterceptAllRequestOnValidCredentialsCompletesFullPipe() {

        Authenticator auth = mock(Authenticator.class);
        when(auth.validateCredentials(anyString(), anyString())).thenReturn(true);

        SessionRepository repository = mock(SessionRepository.class);
        when(repository.contains(anyString())).thenReturn(false);

        Router router = mock(Router.class);
        EmbeddedChannel channel = new EmbeddedChannel(new AuthHandler(auth, repository, router));

        String[] args = {"foo", "bar"};
        CommandRequest req = new CommandRequest("login", args);
        channel.writeInbound(req);

        verify(router, times(1)).accept(any());

        String res = channel.readOutbound();

        Assert.assertTrue(res.equals("Welcome foo"));
    }

    @Test
    public void authMiddlewareBlocksNonLoginRequestsOnUnAuthorizedSession() {
        Authenticator auth = mock(Authenticator.class);
        when(auth.validateCredentials(anyString(), anyString())).thenReturn(true);

        SessionRepository repository = mock(SessionRepository.class);
        when(repository.contains(anyString())).thenReturn(false);

        Router router = mock(Router.class);
        EmbeddedChannel channel = new EmbeddedChannel(new AuthHandler(auth, repository, router));

        String[] args = {"fakeTopic"};
        CommandRequest req = new CommandRequest("join", args);
        channel.writeInbound(req);

        String res = channel.readOutbound();

        Assert.assertTrue(res.equals(AuthHandler.NOT_AUTH_ERROR));
    }

    @Test
    public void authMiddlewareRejectsLoginRequestsOnAlreadyExistsSession() {
        Authenticator auth = mock(Authenticator.class);
        when(auth.validateCredentials(anyString(), anyString())).thenReturn(true);

        SessionRepository repository = mock(SessionRepository.class);
        when(repository.contains(anyString())).thenReturn(true);

        Router router = mock(Router.class);
        EmbeddedChannel channel = new EmbeddedChannel(new AuthHandler(auth, repository, router));

        String[] args = {"foo", "bar"};
        CommandRequest req = new CommandRequest("login", args);
        channel.writeInbound(req);

        String res = channel.readOutbound();

        Assert.assertTrue(res.equals(AuthHandler.USER_ALREADY_EXISTS));
    }

}
