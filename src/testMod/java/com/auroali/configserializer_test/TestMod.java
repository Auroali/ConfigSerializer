package com.auroali.configserializer_test;

import com.auroali.configserializer.ConfigSerializer;
import com.auroali.configserializer.EnumSerializer;
import com.auroali.configserializer.ListSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestMod implements ModInitializer {
    public static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("test.json");
    public static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
    @Override
    public void onInitialize() {
        saveTest();
        loadTest();
    }

    private static void loadTest() {
        JsonObject object;
        try {
            object = GSON.fromJson(Files.readString(CONFIG), JsonObject.class);
        } catch (IOException e) {
            return;
        }

        ConfigSerializer.create(object)
                .readValue("enum", System.out::println, TestEnum.OTHER_TEST, EnumSerializer.createReader(TestEnum.class));
    }

    private static void saveTest() {
        List<String> testList = new ArrayList<>();
        testList.add("test");
        testList.add("Hello!");
        testList.add("Hello world");
        JsonObject root = new JsonObject();
        TestEnum enumTest = TestEnum.TEST;
        ConfigSerializer.create(root)
                .writeValue("listTest", testList, ListSerializer.createWriter(JsonPrimitive::new))
                .writeValue("enum", enumTest, EnumSerializer.createWriter());

        try {
            Files.writeString(CONFIG, GSON.toJson(root));
        } catch (IOException e) {

        }
    }

    public enum TestEnum {
        TEST,
        OTHER_TEST
    }
}
