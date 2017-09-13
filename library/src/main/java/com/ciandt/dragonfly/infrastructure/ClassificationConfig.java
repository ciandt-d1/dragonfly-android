package com.ciandt.dragonfly.infrastructure;

import android.support.annotation.NonNull;

/**
 * Created by iluz on 9/13/17.
 */

public class ClassificationConfig {

    public final static String CLASSIFICATION_ATENUATION_ALGORITHM_NONE = "none";
    public final static String CLASSIFICATION_ATENUATION_ALGORITHM_DECAY = "decay";

    private String classificationAtenuationAlgorithm;
    private float classificationAtenuationDecayDecayValue;
    private float classificationAtenuationDecayUpdateValue;
    private float classificationAtenuationDecayMinimumThreshold;

    private ClassificationConfig(Builder builder) {
        classificationAtenuationAlgorithm = builder.classificationAtenuationAlgorithm;
        classificationAtenuationDecayDecayValue = builder.decayAtenuationDecayValue;
        classificationAtenuationDecayUpdateValue = builder.decayAtenuationUpdateValue;
        classificationAtenuationDecayMinimumThreshold = builder.decayAtenuationMinimumThreshold;
    }

    public String getClassificationAtenuationAlgorithm() {
        return classificationAtenuationAlgorithm;
    }

    public float getClassificationAtenuationDecayDecayValue() {
        return classificationAtenuationDecayDecayValue;
    }

    public float getClassificationAtenuationDecayUpdateValue() {
        return classificationAtenuationDecayUpdateValue;
    }

    public float getClassificationAtenuationDecayMinimumThreshold() {
        return classificationAtenuationDecayMinimumThreshold;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {

        private String classificationAtenuationAlgorithm;

        private float decayAtenuationDecayValue;
        private float decayAtenuationUpdateValue;
        private float decayAtenuationMinimumThreshold;

        private Builder() {
        }

        public Builder withClassificationAtenuationAlgorithm(String classificationAtenuationAlgorithm) {
            this.classificationAtenuationAlgorithm = classificationAtenuationAlgorithm;

            if (!CLASSIFICATION_ATENUATION_ALGORITHM_DECAY.equals(classificationAtenuationAlgorithm)) {
                decayAtenuationDecayValue = 0;
                decayAtenuationUpdateValue = 0;
                decayAtenuationMinimumThreshold = 0;
            }

            return this;
        }

        public Builder withDecayAtenuationDecayValue(float classificationAtenuationDecayDecayValue) {
            validateDecayAlgorithmIsSet();

            this.decayAtenuationDecayValue = classificationAtenuationDecayDecayValue;
            return this;
        }

        public Builder withDecayAtenuationUpdateValue(float classificationAtenuationDecayUpdateValue) {
            validateDecayAlgorithmIsSet();

            this.decayAtenuationUpdateValue = classificationAtenuationDecayUpdateValue;
            return this;
        }

        public Builder withDecayAtenuationMinimumThreshold(float classificationAtenuationDecayMinimumThreshold) {
            validateDecayAlgorithmIsSet();

            this.decayAtenuationMinimumThreshold = classificationAtenuationDecayMinimumThreshold;
            return this;
        }

        @NonNull
        public ClassificationConfig build() {
            return new ClassificationConfig(this);
        }

        private void validateDecayAlgorithmIsSet() {
            if (!CLASSIFICATION_ATENUATION_ALGORITHM_DECAY.equals(classificationAtenuationAlgorithm)) {
                String message = String.format("This argument is only valid for %s algorithm", CLASSIFICATION_ATENUATION_ALGORITHM_DECAY);
                throw new IllegalStateException(message);
            }
        }
    }
}
