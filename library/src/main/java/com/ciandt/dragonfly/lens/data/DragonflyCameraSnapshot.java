package com.ciandt.dragonfly.lens.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflyCameraSnapshot implements Parcelable {

    private String path;
    private int width;
    private int height;

    private DragonflyCameraSnapshot(Builder builder) {
        path = builder.path;
        width = builder.width;
        height = builder.height;
    }

    public String getPath() {
        return path;
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

        private String path;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withPath(String path) {
            this.path = path;
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

        public DragonflyCameraSnapshot build() {
            return new DragonflyCameraSnapshot(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }

    protected DragonflyCameraSnapshot(Parcel in) {
        this.path = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static final Parcelable.Creator<DragonflyCameraSnapshot> CREATOR = new Parcelable.Creator<DragonflyCameraSnapshot>() {

        @Override
        public DragonflyCameraSnapshot createFromParcel(Parcel source) {
            return new DragonflyCameraSnapshot(source);
        }

        @Override
        public DragonflyCameraSnapshot[] newArray(int size) {
            return new DragonflyCameraSnapshot[size];
        }
    };

    @Override
    public String toString() {
        return "DragonflyCameraSnapshot{" +
                "path='" + path + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
