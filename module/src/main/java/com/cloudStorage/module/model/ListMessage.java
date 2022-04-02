package com.cloudStorage.module.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class ListMessage implements CloudMessage {

    private final List<String> files;
    private final String fileDir;

    public ListMessage(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        fileDir = path.normalize().toString();
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.LIST;
    }
}
