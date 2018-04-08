package com.marcosquesada.netty.chat.server.handler;

import com.marcosquesada.netty.chat.command.CommandRequest;
import com.marcosquesada.netty.chat.server.Router;
import com.marcosquesada.netty.chat.server.session.InMemoryRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private Router router;
    private String userName;

    public ClientHandler(String userName, Router router) {
        this.userName = userName;
        this.router = router;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        try{
            router.accept(InMemoryRepository.buildSession(ctx, userName));
            ctx.writeAndFlush(String.format("Welcome %s", userName));
        }catch (Exception e) {
            logger.error("Unexpected exception", e.getMessage());
            ctx.writeAndFlush(e.getMessage());
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        router.close(userName);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            CommandRequest req = (CommandRequest) msg;
            router.receiveMessage(userName, req);
        } finally {
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