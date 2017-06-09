package com.ciandt.dragonfly.data.model;

import java.util.List;

public class ModelManager {

    public static List<Model> loadModels() {
        return ModelRepository.loadModels();
    }

}
