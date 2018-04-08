package com.marcosquesada.netty.chat.server;

import com.marcosquesada.netty.chat.command.CommandExecutor;
import com.marcosquesada.netty.chat.command.Commander;
import com.marcosquesada.netty.chat.executor.Executor;
import com.marcosquesada.netty.chat.executor.Scheduler;
import com.marcosquesada.netty.chat.server.auth.NopAuthenticator;
import com.marcosquesada.netty.chat.server.chat.InMemoryChat;
import com.marcosquesada.netty.chat.server.encoder.CommandDecoder;
import com.marcosquesada.netty.chat.server.command.ChatCommand;
import com.marcosquesada.netty.chat.server.handler.AuthHandler;
import com.marcosquesada.netty.chat.server.handler.ClientHandler;
import com.marcosquesada.netty.chat.server.session.InMemoryRepository;
import com.marcosquesada.netty.chat.server.session.SessionRepository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static String EXECUTOR_WORKER_NAME = "serverExecutor";
    public static Integer EXECUTOR_WORKER_THREADS = 4;
    public static Integer ACCEPTOR_THREADS = 2;
    public static Integer HANDLER_THREADS = 4;

    private EventLoopGroup acceptorGroup;
    private EventLoopGroup handlerGroup;
    private ChannelFuture channelFuture;
    private Router router;
    private SessionRepository repository;
    private Integer port;

    public Server(Integer port) {
        this.port = port;

        InMemoryChat broker = new InMemoryChat(InMemoryChat.MAX_USERS_BY_ROOM, InMemoryChat.HISTORY_SIZE);
        repository = new InMemoryRepository();
        ChatCommand cmds = new ChatCommand(broker, repository);
        Commander commandExecutor = new CommandExecutor();
        commandExecutor.register(cmds);

        Executor executor = new Scheduler(EXECUTOR_WORKER_NAME, EXECUTOR_WORKER_THREADS);
        router = new Router(broker, commandExecutor, executor, repository);
    }

    public void start() {
        acceptorGroup = new NioEventLoopGroup(ACCEPTOR_THREADS);
        handlerGroup = new NioEventLoopGroup(HANDLER_THREADS);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptorGroup, handlerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress("localhost", port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
                            pipeline.addLast("stringDecoder", new CommandDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast("lineEncoder", new LineEncoder(LineSeparator.UNIX, CharsetUtil.UTF_8));
                            pipeline.addLast("authHandler", new AuthHandler(new NopAuthenticator(),repository,  router));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 5) //@TODO: refuse connections if it already has 5 queued
                    .childOption(ChannelOption.SO_KEEPALIVE, true);// keep their connections open with keepalive packets.

            channelFuture = serverBootstrap.bind().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        try {
            acceptorGroup.shutdownGracefully().sync();
            handlerGroup.shutdownGracefully().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Terminate exception " , e.getMessage());
        }
    }
}
