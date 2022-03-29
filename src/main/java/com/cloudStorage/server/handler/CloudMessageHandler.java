package com.cloudStorage.server.handler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.cloudStorage.server.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class CloudMessageHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path serverDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverDir = Paths.get("serverDir");
        ctx.write(new ListMessage(serverDir));
        ctx.writeAndFlush(new FileDir(serverDir.toFile().getPath()));
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
                ctx.write(new FileMessage(serverDir.resolve(fr.getName())));
                if (fr.isDelete()) {
                    deleteFile(serverDir.resolve(fr.getName()).toFile(), fr.getName(), fr.isDelete());
                }
                ctx.writeAndFlush(new ListMessage(serverDir));
                break;
        }
    }

    private void deleteFile(File file, String name, boolean delete) {
        if (delete) {
            if (file.delete()) {
                System.out.println("File: " + name + " delete");
            }
        }
    }
}
