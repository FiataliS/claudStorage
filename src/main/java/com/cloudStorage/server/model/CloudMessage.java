package com.cloudStorage.server.model;

import java.io.Serializable;

public interface CloudMessage extends Serializable {
    MessageType getMessageType();
}
