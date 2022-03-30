package com.cloudStorage.server.handler;

import com.cloudStorage.server.model.*;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.cloudStorage.server.model.MessageType.*;

public class HandlerOperation {
    private static Path serverDir = CloudMessageHandler.getServerDir();
    ;

    public static final Map<MessageType, BiConsumer<ChannelHandlerContext, CloudMessage>> HANDLER_MAP = new HashMap<>();

    static {
        HANDLER_MAP.put(FILE, (ctx, cloudMessage) -> {
            try {
                FileMessage fm = (FileMessage) cloudMessage;
                Files.write(serverDir.resolve(fm.getName()), fm.getBytes());
                ctx.writeAndFlush(new ListMessage(serverDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        HANDLER_MAP.put(FILE_REQUEST, (ctx, cloudMessage) -> {
            try {
                FileRequest fr = (FileRequest) cloudMessage;
                ctx.write(new FileMessage(serverDir.resolve(fr.getName())));
                if (fr.isDelete()) {
                    deleteFile(serverDir.resolve(fr.getName()).toFile(), fr.getName(), fr.isDelete());
                }
                ctx.writeAndFlush(new ListMessage(serverDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        HANDLER_MAP.put(LIST, (ctx, cloudMessage) -> {
            try {
                ctx.writeAndFlush(new ListMessage(serverDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void deleteFile(File file, String name, boolean delete) {
        if (delete) {
            if (file.delete()) {
                System.out.println("File: " + name + " delete");
            }
        }
    }
}
