package com.ciandt.dragonfly.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FakeModelGenerator {

    public static Model generate(String id) {

        Model model = new Model(id);

        model.setName(getName(id));
        model.setVersion(getVersion(id));
        model.setSize(getSize(id));
        model.setDescription(getDescription(id));
        model.setColors(getColors(id));
        model.setReady(id.equals("1"));

        return model;
    }

    private static String getName(String id) {

        Map<String, String> names = new HashMap<>();
        names.put("1", "Automotive");
        names.put("2", "Clothes");
        names.put("3", "Flowers");
        names.put("4", "Dogs");
        names.put("5", "Employees");
        names.put("6", "Foods");

        if (names.containsKey(id)) {
            return names.get(id);
        }

        return UUID.randomUUID().toString();
    }

    private static int getVersion(String id) {

        Map<String, Integer> versions = new HashMap<>();
        versions.put("1", 1);
        versions.put("2", 2);
        versions.put("3", 1);
        versions.put("4", 3);
        versions.put("5", 12);
        versions.put("6", 2);

        if (versions.containsKey(id)) {
            return versions.get(id);
        }

        return (new Random().nextInt(100) + 1);
    }

    private static long getSize(String id) {

        Map<String, Long> versions = new HashMap<>();
        versions.put("1", 31457280L);
        versions.put("2", 52428800L);
        versions.put("3", 15728640L);
        versions.put("4", 20447232L);
        versions.put("5", 106954752L);
        versions.put("6", 48234493L);

        if (versions.containsKey(id)) {
            return versions.get(id);
        }

        return (new Random().nextInt(50000000) + 10000000L);
    }

    private static String getDescription(String id) {

        Map<String, String> names = new HashMap<>();
        names.put("1", "More than +20000 types automotive pieces, brands and model.");
        names.put("2", "Recognize more than +3000 models of clothes, looks and styles.");
        names.put("3", "This model recognize more than 5000 flowers species.");
        names.put("4", "Discover the breed of your dog. More than 200 breeds.");
        names.put("5", "Discover the name of your employees. On demand.");
        names.put("6", "Is your food healthy? What is the nutritional information?");

        if (names.containsKey(id)) {
            return names.get(id);
        }

        return UUID.randomUUID().toString();
    }

    private static String[] getColors(String id) {

        Map<String, String[]> colors = new HashMap<>();

        colors.put("1", new String[]{"#8E78FD", "#826BF2"});
        colors.put("2", new String[]{"#DE7088", "#F5A293"});
        colors.put("3", new String[]{"#3EB9C1", "#22E2B8"});
        colors.put("4", new String[]{"#e9d362", "#333333"});
        colors.put("5", new String[]{"#9d50bb", "#6e48aa"});
        colors.put("7", new String[]{"#73c8a9", "#373b44"});

        if (colors.containsKey(id)) {
            return colors.get(id);
        }

        Random random = new Random();
        String from = String.format("#%06X", random.nextInt(0x1000000));
        String to = String.format("#%06X", random.nextInt(0x1000000));

        return new String[]{from, to};
    }
}