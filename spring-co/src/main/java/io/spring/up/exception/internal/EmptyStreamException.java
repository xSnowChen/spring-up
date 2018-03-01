package io.spring.up.exception.internal;

import java.text.MessageFormat;

public class EmptyStreamException extends AbstractInternalException {

    public EmptyStreamException(final String message) {
        super(MessageFormat.format(Message.EMPTY_STREAM, message), -10003);
    }

    @Override
    public int getCode() {
        return -10003;
    }
}