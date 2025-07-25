package com.justin.model;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ObjectDeserializer;

import java.lang.reflect.Type;

public class MessageTypeDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        String messageType = defaultJSONParser.parseObject(String.class);
        return (T) MessageType.valueOf(messageType);
    }
}
