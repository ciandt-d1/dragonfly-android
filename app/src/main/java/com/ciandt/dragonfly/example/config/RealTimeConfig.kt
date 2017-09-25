package com.ciandt.dragonfly.example.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * Created by iluz on 9/12/17.
 */
class RealTimeConfig {
    companion object {
        val CLASSIFICATION_ATENUATION_ALGORITHM = FirebaseRemoteConfig.getInstance().getString("classification_optimization_algorithm")

        val CLASSIFICATION_ATENUATION_ALGORITHM_DECAY__DECAY_VALUE = FirebaseRemoteConfig.getInstance().getDouble("classification_optimization_algorithm_decay__decay_value").toFloat()
        val CLASSIFICATION_ATENUATION_ALGORITHM_DECAY__UPDATE_VALUE = FirebaseRemoteConfig.getInstance().getDouble("classification_optimization_algorithm_decay__update_value").toFloat()
        val CLASSIFICATION_ATENUATION_ALGORITHM_DECAY__MINIMUM_THRESHOLD = FirebaseRemoteConfig.getInstance().getDouble("classification_optimization_algorithm_decay__minimum_threshold").toFloat()

        val UNCOMPRESSED_MODEL_SIZE_CALCULATION_FACTOR = FirebaseRemoteConfig.getInstance().getDouble("real_time__uncompressed_model_size_calculation_factor").toFloat()
    }
}