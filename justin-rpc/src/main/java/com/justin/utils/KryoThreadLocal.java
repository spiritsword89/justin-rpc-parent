package com.justin.utils;

import com.esotericsoftware.kryo.Kryo;

// Kryo is not thread safe
// Creating new Kryo object is very expensive
public class KryoThreadLocal {
    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    public static Kryo get() {
        return KRYO_LOCAL.get();
    }

    public static void remove() {
        KRYO_LOCAL.remove();
    }
}
