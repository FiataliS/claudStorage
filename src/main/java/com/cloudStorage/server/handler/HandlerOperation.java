package com.cloudStorage.server.handler;

import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.cloudStorage.module.model.*;

import static com.cloudStorage.module.model.MessageType.*;


public class HandlerOperation {
    private static Path serverDir;

    public static Path getServerDir() {
        return serverDir;
    }

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

        HANDLER_MAP.put(AUTH_SERV, (ctx, cloudMessage) -> {
            AuthServ authServ = (AuthServ) cloudMessage;
            AuthService.connect();
            String id = AuthService.getID(authServ.getNick(), authServ.getPass());
            if (id != null) {
                serverDir = Paths.get("serverDir");
                File selected = serverDir.resolve(id).toFile();
                serverDir = selected.toPath();
                ctx.writeAndFlush(new AuthServ(authServ.getNick(), "_", true));
            } else {
                System.out.println("Пороль не правильный");
                ctx.writeAndFlush(new AuthServ(authServ.getNick(), "_", false));
            }
            AuthService.disconnect();
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
