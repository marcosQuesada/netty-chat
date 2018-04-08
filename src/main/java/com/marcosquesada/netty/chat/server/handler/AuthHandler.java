package com.marcosquesada.netty.chat.server.handler;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.server.Router;
import com.marcosquesada.netty.chat.server.auth.Authenticator;
import com.marcosquesada.netty.chat.server.session.SessionRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    public static String LOGIN_COMMAND = "login";

    public static String NOT_AUTH_ERROR = "Not Authorized, you must log in";
    public static String INVALID_REQUEST = "Invalid Request";
    public static String USER_ALREADY_EXISTS = "User already exists";
    public static String INVALID_CREDENTIALS = "Invalid Credentials";

    private Authenticator authenticator;
    private Boolean authorized = false;
    private SessionRepository repository;
    private Router router;

    public AuthHandler(Authenticator authenticator, SessionRepository repository, Router router) {
        this.authenticator = authenticator;
        this.repository = repository;
        this.router = router;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CommandRequest request = (CommandRequest) msg;

        try {
            if (authorized) {
                ctx.fireChannelRead(msg);
                return;
            }

            if (!request.getCmd().equals(LOGIN_COMMAND)) {
                ctx.writeAndFlush(NOT_AUTH_ERROR);
                return;
            }

            if (request.getArguments().length != 2) {
                ctx.writeAndFlush(INVALID_REQUEST);
                return;
            }

            String userName = request.getArguments()[0];
            String pass = request.getArguments()[1];

            if (repository.contains(userName)) {
                ctx.writeAndFlush(USER_ALREADY_EXISTS);
                ctx.close();

                return;
            }

            if (!authenticator.validateCredentials(userName, pass)) {
                ctx.writeAndFlush(INVALID_CREDENTIALS);

                return;
            }

            authorized = true;
            ctx.pipeline().addLast(new ClientHandler(userName, router));
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught ", cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

}