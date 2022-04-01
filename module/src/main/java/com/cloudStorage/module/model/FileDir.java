package com.cloudStorage.module.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileDir implements CloudMessage {

    private final Path fileDir;
    private final String item;

    public FileDir(Path path, String item) throws IOException {
        this.fileDir = path;
        this.item = item;
    }

    public Path getFileDir() {
        return fileDir;
    }

    public String getItem() {
        return item;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_DIR;
    }
}
