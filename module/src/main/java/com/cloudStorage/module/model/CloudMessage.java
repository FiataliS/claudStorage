package com.cloudStorage.module.model;

import java.io.Serializable;

public interface CloudMessage extends Serializable {
    MessageType getMessageType();
}
