package com.cloudStorage.server.model;

public class FileDir implements CloudMessage{

    private final String dir;

    public FileDir (String dir){
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_DIR;
    }
}
