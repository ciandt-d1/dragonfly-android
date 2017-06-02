package com.ciandt.dragonfly.data;

import java.util.List;

public class ModelManager {

    public static List<Model> loadModels() {
        return ModelRepository.loadModels();
    }

}
