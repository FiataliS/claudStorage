package com.cloudStorage.module.model;

import java.io.IOException;

public class NewFolder implements CloudMessage {


        private final String item;

        public NewFolder(String item) throws IOException {
                this.item = item;
        }

        public String getItem() {
                return item;
        }

        @Override
        public MessageType getMessageType() {return MessageType.NEW_FOLDER;}
}
