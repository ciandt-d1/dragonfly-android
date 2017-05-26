package com.ciandt.dragonfly.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class Model implements Parcelable {

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_DOWNLOADED = 2;

    private String id;
    private String name;
    private int version;
    private long size;
    private String description;
    private String[] colors;
    private int status = STATUS_DEFAULT;


    public Model(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDownloading() {
        return status == STATUS_DOWNLOADING;
    }

    public boolean isDownloaded() {
        return status == STATUS_DOWNLOADED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        return id != null ? id.equals(model.id) : model.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Model {" +
                "\n\tid='" + id + '\'' +
                ",\n\tname='" + name + '\'' +
                ",\n\tversion=" + version +
                ",\n\tsize=" + size +
                ",\n\tdescription='" + description + '\'' +
                ",\n\tcolors=" + Arrays.toString(colors) +
                ",\n\tstatus=" + status +
                "\n}";
    }

    /**
     * Parcelable methods
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.version);
        dest.writeLong(this.size);
        dest.writeString(this.description);
        dest.writeStringArray(this.colors);
        dest.writeInt(this.status);
    }

    protected Model(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.version = in.readInt();
        this.size = in.readLong();
        this.description = in.readString();
        this.colors = in.createStringArray();
        this.status = in.readInt();
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {

        @Override
        public Model createFromParcel(Parcel source) {
            return new Model(source);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };
}
