package com.cloudStorage.server.handler;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.cloudStorage.server.service.UserNameService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringEchoHandler extends SimpleChannelInboundHandler<String> {

    private final UserNameService userNameService;
    private final ConcurrentLinkedDeque<ChannelHandlerContext> users;
    private final String userName;

    public StringEchoHandler(
            UserNameService userNameService,
            ConcurrentLinkedDeque<ChannelHandlerContext> users
    ) {
        this.userNameService = userNameService;
        this.users = users;
        userName = userNameService.getUserName();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected...");
        users.add(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected...");
        users.remove(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error: ", cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        log.debug("Received: {}", s);
        // ctx.writeAndFlush(userName + ": " + s);
        for (ChannelHandlerContext user : users) {
            user.writeAndFlush(userName + ": " + s);
        }
    }
}
