package com.ciandt.dragonfly.lens.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflyClassificationInput implements Parcelable {

    private final String imagePath;
    private final int width;
    private final int height;

    private DragonflyClassificationInput(Builder builder) {
        imagePath = builder.imagePath;
        width = builder.width;
        height = builder.height;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private String imagePath;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withImagePath(String path) {
            this.imagePath = path;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public DragonflyClassificationInput build() {
            return new DragonflyClassificationInput(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imagePath);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }

    protected DragonflyClassificationInput(Parcel in) {
        this.imagePath = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static final Parcelable.Creator<DragonflyClassificationInput> CREATOR = new Parcelable.Creator<DragonflyClassificationInput>() {

        @Override
        public DragonflyClassificationInput createFromParcel(Parcel source) {
            return new DragonflyClassificationInput(source);
        }

        @Override
        public DragonflyClassificationInput[] newArray(int size) {
            return new DragonflyClassificationInput[size];
        }
    };

    @Override
    public String toString() {
        return "DragonflyClassificationInput{" +
                "imagePath='" + imagePath + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
