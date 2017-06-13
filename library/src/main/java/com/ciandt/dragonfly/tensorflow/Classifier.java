/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.ciandt.dragonfly.tensorflow;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    class Recognition implements Parcelable {

        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance
         * of
         * the object.
         */
        private final String id;

        /**
         * Display name for the recognition.
         */
        private final String title;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be
         * better.
         */
        private final Float confidence;

        /** Optional location within the source image for the location of the recognized object. */
        private RectF location;

        public Recognition(
                final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public boolean hasTitle() {
            return (title != null) && (title.length() > 0);
        }

        public boolean isRelevant(float threshold) {
            return (confidence != null) && (confidence >= threshold);
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }

            if (title != null) {
                resultString += title + " ";
            }

            if (confidence != null) {
                resultString += confidence;
            }

            if (location != null) {
                resultString += location + " ";
            }

            return resultString.trim();
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.title);
            dest.writeValue(this.confidence);
            dest.writeParcelable(this.location, flags);
        }

        protected Recognition(Parcel in) {
            this.id = in.readString();
            this.title = in.readString();
            this.confidence = (Float) in.readValue(Float.class.getClassLoader());
            this.location = in.readParcelable(RectF.class.getClassLoader());
        }

        public static final Parcelable.Creator<Recognition> CREATOR = new Parcelable.Creator<Recognition>() {

            @Override
            public Recognition createFromParcel(Parcel source) {
                return new Recognition(source);
            }

            @Override
            public Recognition[] newArray(int size) {
                return new Recognition[size];
            }
        };
    }

    List<Recognition> recognizeImage(Bitmap bitmap);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();
}

