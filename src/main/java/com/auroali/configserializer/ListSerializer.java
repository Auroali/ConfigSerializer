package com.auroali.configserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListSerializer {
    public static <T> Writer<T> createWriter(Function<T, JsonElement> conversionFunc) {
        return new Writer<>(conversionFunc);
    }

    public static <T> Reader<T> createReader(ConfigSerializer.Reader<T> valueReader, Supplier<List<T>> listFactory) {
        return new Reader<>(valueReader, listFactory);
    }

    public static <T> Reader<T> createReader(ConfigSerializer.Reader<T> valueReader) {
        return createReader(valueReader, ArrayList::new);
    }

    public static class Writer<T> implements ConfigSerializer.Writer<List<T>> {
        Function<T, JsonElement> valueWriter;

        private Writer(Function<T, JsonElement> conversionFunc) {
            this.valueWriter = conversionFunc;
        }

        @Override
        public void write(JsonObject object, String name, List<T> value) {
            JsonArray array = new JsonArray();
            for(T val : value) {
                array.add(valueWriter.apply(val));
            }
            object.add(name, array);
        }
    }
    public static class Reader<T> implements ConfigSerializer.Reader<List<T>> {
        ConfigSerializer.Reader<T> valueReader;
        Supplier<List<T>> listSupplier;
        private Reader(ConfigSerializer.Reader<T> valueReader, Supplier<List<T>> listSupplier) {
            this.valueReader = valueReader;
            this.listSupplier = listSupplier;
        }

        @Override
        public List<T> read(JsonElement object) {
            JsonArray array = object.getAsJsonArray();
            List<T> list = listSupplier.get();
            for(JsonElement element : array) {
                T deserialized = valueReader.read(element);
                list.add(deserialized);
            }
            return list;
        }
    }
}
