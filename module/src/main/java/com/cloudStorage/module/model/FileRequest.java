package com.cloudStorage.module.model;

import lombok.Data;

@Data
public class FileRequest implements CloudMessage {

    private final String name;
    private final boolean delete;

    public FileRequest(String name, boolean delete) {
        this.name = name;
        this.delete = delete;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_REQUEST;
    }



}
