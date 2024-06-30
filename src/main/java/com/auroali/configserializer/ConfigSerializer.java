package com.auroali.configserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigSerializer {
    private final static Logger LOGGER = LoggerFactory.getLogger("Config Serializer");
    private final ConfigSerializer parent;
    private final ConfigSerializer root;
    private final JsonObject obj;
    private final String category;
    boolean shouldResaveConfig;

    public ConfigSerializer(ConfigSerializer parent, JsonObject obj, ConfigSerializer root, String category) {
        this.parent = parent;
        this.obj = obj;
        this.root = root;
        this.category = category;
    }

    public ConfigSerializer category(String name) {
        if(obj.has(name))
            return new ConfigSerializer(this, obj.getAsJsonObject(name), root != null ? root : this, name);

        JsonObject object = new JsonObject();
        obj.add(name, object);
        return new ConfigSerializer(this, object, root != null ? root : this, name);
    }

    public ConfigSerializer up() {
        if(parent == null)
            throw new IllegalStateException("Cannot go up from root!");
        return parent;
    }

    public <T> ConfigSerializer writeValue(String name, T value, Writer<T> writer) {
        writer.write(obj, name, value);
        return this;
    }

    private String getFullCategoryName() {
        StringBuilder builder = new StringBuilder(this.category);
        ConfigSerializer parent = this.parent;
        while(parent != null) {
            builder.insert(0, parent.category + "/");
            parent = parent.parent;
        }
        return builder.toString();
    }

    public <T> ConfigSerializer readValue(String name, Consumer<T> valueWriter, T defaultVal, Reader<T> conversionFunc) {
        if(obj.has(name)) {
            try {
                valueWriter.accept(conversionFunc.read(obj.get(name)));
                return this;
            } catch (Exception e) {
                LOGGER.error("An error occurred whilst reading config key {}/{}. Falling back to default value.", getFullCategoryName(), name, e);
            }
        } else LOGGER.warn("Missing config key {}/{}, using default", getFullCategoryName(), name);
        if(root != null)
            root.shouldResaveConfig = true;
        else
            shouldResaveConfig = true;
        valueWriter.accept(defaultVal);
        return this;
    }

    public void saveIfNeeded(Runnable runnable) {
        if(root != null)
            throw new IllegalStateException("Cannot save config on non-root node! Expected '/', got '%s'!".formatted(getFullCategoryName()));
        if(runnable == null)
            throw new NullPointerException("Cannot save config with null runnable!");
        if(shouldResaveConfig)
            runnable.run();
    }

    public static ConfigSerializer create(JsonObject root) {
        return new ConfigSerializer(null, root, null, "");
    }

    @FunctionalInterface
    public interface Writer<T> {
        void write(JsonObject object, String name, T value);
    }

    @FunctionalInterface
    public interface Reader<T> {
        T read(JsonElement object);
    }

    public static class ListWriter<T> implements Writer<List<T>> {
        private final Function<T, JsonElement> writer;

        protected ListWriter(Function<T, JsonElement> propertyWriter) {
            this.writer = propertyWriter;
        }

        public static <T> ListWriter<T> create(Function<T, JsonElement> propertyWriter) {
            return new ListWriter<>(propertyWriter);
        }

        @Override
        public void write(JsonObject object, String name, List<T> value) {
            JsonArray array = new JsonArray(value.size());
            for(T val : value) {
                array.add(writer.apply(val));
            }
            object.add(name, array);
        }
    }
    public static class ListReader<T> implements Reader<List<T>> {
        private final Function<JsonElement, T> reader;

        protected ListReader(Function<JsonElement, T> propertyReader) {
            this.reader = propertyReader;
        }

        public static <T> ListReader<T> create(Function<JsonElement, T> propertyReader) {
            return new ListReader<>(propertyReader);
        }

        @Override
        public List<T> read(JsonElement object) {
            JsonArray arr = object.getAsJsonArray();
            List<T> valueList = new ArrayList<>(arr.size());
            for(JsonElement element : arr) {
                valueList.add(reader.apply(element));
            }
            return valueList;
        }
    }
}
