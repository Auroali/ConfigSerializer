package com.auroali.configserializer;

import java.util.Locale;

public class EnumSerializer {
    public static <T extends Enum<T>> ConfigSerializer.Reader<T> createReader(Class<T> e) {
        return val -> Enum.valueOf(e, val.getAsString());
    }

    public static <T extends Enum<T>> ConfigSerializer.Writer<T> createWriter() {
        return (object, name, value) -> object.addProperty(name, value.name());
    }
}
