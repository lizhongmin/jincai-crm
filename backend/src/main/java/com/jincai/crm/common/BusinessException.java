package com.jincai.crm.common;

public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final Object[] messageArgs;

    public BusinessException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs == null ? new Object[0] : messageArgs.clone();
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}

