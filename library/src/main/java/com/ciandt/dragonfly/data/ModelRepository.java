package com.ciandt.dragonfly.data;

import java.util.ArrayList;
import java.util.List;

class ModelRepository {

    static List<Model> loadModels() {
        ArrayList<Model> models = new ArrayList<>();
        models.add(FakeModelGenerator.generate("1"));
        models.add(FakeModelGenerator.generate("2"));
        models.add(FakeModelGenerator.generate("3"));
        models.add(FakeModelGenerator.generate("4"));
        models.add(FakeModelGenerator.generate("5"));
        models.add(FakeModelGenerator.generate("6"));
        return models;
    }
}
