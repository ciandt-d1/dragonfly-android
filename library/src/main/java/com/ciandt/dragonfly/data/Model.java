package com.ciandt.dragonfly.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.Arrays;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class Model implements Parcelable {

    @Retention(SOURCE)
    @IntDef({STATUS_DEFAULT, STATUS_DOWNLOADING, STATUS_DOWNLOADED})
    public @interface Status {

    }

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_DOWNLOADED = 2;

    @Status
    private int status = STATUS_DEFAULT;

    private final String id;
    private String name;
    private int version;
    private long size;
    private String description;
    private String[] colors;


    private String modelPath;
    private String labelsPath;

    private int inputSize;
    private int imageMean;
    private float imageStd;
    private String inputName;
    private String outputName;

    public Model(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Model setName(String name) {
        this.name = name;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public Model setVersion(int version) {
        this.version = version;
        return this;
    }

    public long getSize() {
        return size;
    }

    public Model setSize(long size) {
        this.size = size;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Model setDescription(String description) {
        this.description = description;
        return this;
    }

    public String[] getColors() {
        return colors;
    }

    public Model setColors(String[] colors) {
        this.colors = colors;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Model setStatus(@Status int status) {
        this.status = status;
        return this;
    }

    public boolean isDownloading() {
        return status == STATUS_DOWNLOADING;
    }

    public boolean isDownloaded() {
        return status == STATUS_DOWNLOADED;
    }


    public String getModelPath() {
        return modelPath;
    }

    public Model setModelPath(String modelPath) {
        this.modelPath = modelPath;
        return this;
    }

    public String getLabelsPath() {
        return labelsPath;
    }

    public Model setLabelsPath(String labelsPath) {
        this.labelsPath = labelsPath;
        return this;
    }

    public int getInputSize() {
        return inputSize;
    }

    public Model setInputSize(int inputSize) {
        this.inputSize = inputSize;
        return this;
    }

    public int getImageMean() {
        return imageMean;
    }

    public Model setImageMean(int imageMean) {
        this.imageMean = imageMean;
        return this;
    }

    public float getImageStd() {
        return imageStd;
    }

    public Model setImageStd(float imageStd) {
        this.imageStd = imageStd;
        return this;
    }

    public String getInputName() {
        return inputName;
    }

    public Model setInputName(String inputName) {
        this.inputName = inputName;
        return this;
    }

    public String getOutputName() {
        return outputName;
    }

    public Model setOutputName(String outputName) {
        this.outputName = outputName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Model model = (Model) o;

        return id != null ? id.equals(model.id) : model.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version=" + version +
                ", size=" + size +
                ", description='" + description + '\'' +
                ", colors=" + Arrays.toString(colors) +
                ", status=" + status +
                ", modelPath='" + modelPath + '\'' +
                ", labelsPath='" + labelsPath + '\'' +
                ", inputSize=" + inputSize +
                ", imageMean=" + imageMean +
                ", imageStd=" + imageStd +
                ", inputName='" + inputName + '\'' +
                ", outputName='" + outputName + '\'' +
                '}';
    }

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
        dest.writeString(this.modelPath);
        dest.writeString(this.labelsPath);
        dest.writeInt(this.inputSize);
        dest.writeInt(this.imageMean);
        dest.writeFloat(this.imageStd);
        dest.writeString(this.inputName);
        dest.writeString(this.outputName);
    }

    protected Model(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.version = in.readInt();
        this.size = in.readLong();
        this.description = in.readString();
        this.colors = in.createStringArray();
        this.status = in.readInt();
        this.modelPath = in.readString();
        this.labelsPath = in.readString();
        this.inputSize = in.readInt();
        this.imageMean = in.readInt();
        this.imageStd = in.readFloat();
        this.inputName = in.readString();
        this.outputName = in.readString();
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
