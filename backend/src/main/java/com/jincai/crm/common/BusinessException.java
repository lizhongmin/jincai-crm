package com.jincai.crm.common;

import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private final String messageKey;
    private final Object[] messageArgs;

    public BusinessException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}

