package com.cloudStorage.server.handler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.cloudStorage.server.model.CloudMessage;
import com.cloudStorage.server.model.FileRequest;
import com.cloudStorage.server.model.ListMessage;
import com.cloudStorage.server.model.FileMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class CloudMessageHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Paths.get("server");
        ctx.writeAndFlush(new ListMessage(serverDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getMessageType()) {
            case FILE:
                FileMessage fm = (FileMessage) cloudMessage;
                Files.write(serverDir.resolve(fm.getName()), fm.getBytes());
                ctx.writeAndFlush(new ListMessage(serverDir));
                break;
            case FILE_REQUEST:
                FileRequest fr = (FileRequest) cloudMessage;
                ctx.writeAndFlush(new FileMessage(serverDir.resolve(fr.getName())));
                break;
        }
    }
}
