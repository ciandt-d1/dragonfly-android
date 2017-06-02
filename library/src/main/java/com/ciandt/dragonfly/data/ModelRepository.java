package com.ciandt.dragonfly.data;

import java.util.ArrayList;
import java.util.List;

class ModelRepository {

    static List<Model> loadModels() {
        ArrayList<Model> models = new ArrayList<>();
        models.add(FakeModelGenerator.generate("1"));
        models.add(FakeModelGenerator.generate("2"));
        models.add(FakeModelGenerator.generate("3"));
        return models;
    }
}
