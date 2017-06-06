package com.ciandt.dragonfly.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

class FakeModelGenerator {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "file:///android_asset/models/1/model.pb";
    private static final String LABEL_FILE = "file:///android_asset/model/1/labels.txt";

    public static Model generate(String id) {

        if (id.equals("1")) {
            return getFlowersModel();
        }

        if (id.equals("2")) {
            return getTensorflowDemoModel();
        }

        if (id.equals("3")) {
            return getHPModel();
        }

        return new Model(id)
                .setName(getName(id))
                .setVersion(getVersion(id))
                .setSize(getSize(id))
                .setDescription(getDescription(id))
                .setColors(getColors(id))
                .setInputSize(INPUT_SIZE)
                .setImageMean(IMAGE_MEAN)
                .setImageStd(IMAGE_STD)
                .setInputName(INPUT_NAME)
                .setOutputName(OUTPUT_NAME)
                .setModelPath(MODEL_FILE)
                .setLabelsPath(LABEL_FILE);
    }

    private static Model getFlowersModel() {
        return new Model("1")
                .setName("Flowers")
                .setVersion(1)
                .setSize(88481067L)
                .setDescription("This model recognize 130 plants")
                .setColors(new String[]{"#9BCE4F", "#228B22"})
                .setInputSize(299)
                .setImageMean(128)
                .setImageStd(128)
                .setInputName("Mul")
                .setOutputName("final_result")
                .setModelPath("file:///android_asset/models/flowers/model.pb")
                .setLabelsPath("file:///android_asset/models/flowers/labels.txt")
                .setStatus(Model.STATUS_DOWNLOADED);
    }

    private static Model getTensorflowDemoModel() {
        return new Model("2")
                .setName("TF Demo")
                .setVersion(1)
                .setSize(53884595L)
                .setDescription("Simple model for demonstration")
                .setColors(new String[]{"#8E78FD", "#826BF2"})
                .setInputSize(224)
                .setImageMean(117)
                .setImageStd(1)
                .setInputName("input")
                .setOutputName("output")
                .setModelPath("file:///android_asset/models/demo/model.pb")
                .setLabelsPath("file:///android_asset/models/demo/labels.txt")
                .setStatus(Model.STATUS_DOWNLOADED);
    }

    private static Model getHPModel() {
        return new Model("3")
                .setName("Sorting Hat")
                .setVersion(1)
                .setSize(87164854L)
                .setDescription("Which Hogwarts house would you be sorted into?")
                .setColors(new String[]{"#FDD736", "#990000"})
                .setInputSize(299)
                .setImageMean(128)
                .setImageStd(128)
                .setInputName("Mul")
                .setOutputName("final_result")
                .setModelPath("file:///android_asset/models/hp/model.pb")
                .setLabelsPath("file:///android_asset/models/hp/labels.txt")
                .setStatus(Model.STATUS_DOWNLOADED);
    }

    private static String getName(String id) {

        Map<String, String> names = new HashMap<>();
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