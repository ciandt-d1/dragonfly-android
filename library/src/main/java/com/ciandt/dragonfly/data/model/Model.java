package com.ciandt.dragonfly.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Model implements Parcelable {

    private static final String SEPARATOR = ",";

    private String id = "";
    private int version = 0;

    private String modelPath = "";
    private String labelsPath = "";

    private long sizeInBytes = 0;

    private int inputSize = 0;
    private int imageMean = 0;
    private float imageStd = 0f;
    private String inputName = "";
    private String outputName = "";

    private Map<String, Parcelable> others = new HashMap<>();


    public Model(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public Model setVersion(int version) {
        this.version = version;
        return this;
    }

    public String getModelPath() {
        return modelPath;
    }

    public Model setModelPath(String modelPath) {
        this.modelPath = modelPath;
        return this;
    }

    public String[] getLabelsPath() {
        return labelsPath.split(SEPARATOR);
    }

    public Model setLabelsPath(String labelsPath) {
        this.labelsPath = labelsPath;
        return this;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public Model setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
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

    public String[] getOutputName() {
        return outputName.split(SEPARATOR);
    }

    public Model setOutputName(String outputName) {
        this.outputName = outputName;
        return this;
    }

    public Map<String, Parcelable> getOthers() {
        return others;
    }

    public Model setOthers(Map<String, Parcelable> others) {
        this.others = others;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        if (version != model.version) return false;
        if (sizeInBytes != model.sizeInBytes) return false;
        if (inputSize != model.inputSize) return false;
        if (imageMean != model.imageMean) return false;
        if (Float.compare(model.imageStd, imageStd) != 0) return false;
        if (id != null ? !id.equals(model.id) : model.id != null) return false;
        if (modelPath != null ? !modelPath.equals(model.modelPath) : model.modelPath != null)
            return false;
        if (labelsPath != null ? !labelsPath.equals(model.labelsPath) : model.labelsPath != null)
            return false;
        if (inputName != null ? !inputName.equals(model.inputName) : model.inputName != null)
            return false;
        if (outputName != null ? !outputName.equals(model.outputName) : model.outputName != null)
            return false;
        return others != null ? others.equals(model.others) : model.others == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (modelPath != null ? modelPath.hashCode() : 0);
        result = 31 * result + (labelsPath != null ? labelsPath.hashCode() : 0);
        result = 31 * result + Long.valueOf(sizeInBytes).hashCode();
        result = 31 * result + inputSize;
        result = 31 * result + imageMean;
        result = 31 * result + (imageStd != +0.0f ? Float.floatToIntBits(imageStd) : 0);
        result = 31 * result + (inputName != null ? inputName.hashCode() : 0);
        result = 31 * result + (outputName != null ? outputName.hashCode() : 0);
        result = 31 * result + (others != null ? others.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", modelPath='" + modelPath + '\'' +
                ", labelsPath='" + labelsPath + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                ", inputSize=" + inputSize +
                ", imageMean=" + imageMean +
                ", imageStd=" + imageStd +
                ", inputName='" + inputName + '\'' +
                ", outputName='" + outputName + '\'' +
                ", others='" + others + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.version);
        dest.writeString(this.modelPath);
        dest.writeString(this.labelsPath);
        dest.writeLong(this.sizeInBytes);
        dest.writeInt(this.inputSize);
        dest.writeInt(this.imageMean);
        dest.writeFloat(this.imageStd);
        dest.writeString(this.inputName);
        dest.writeString(this.outputName);
        dest.writeInt(this.others.size());
        for (Map.Entry<String, Parcelable> entry : this.others.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    protected Model(Parcel in) {
        this.id = in.readString();
        this.version = in.readInt();
        this.modelPath = in.readString();
        this.labelsPath = in.readString();
        this.sizeInBytes = in.readLong();
        this.inputSize = in.readInt();
        this.imageMean = in.readInt();
        this.imageStd = in.readFloat();
        this.inputName = in.readString();
        this.outputName = in.readString();
        int othersSize = in.readInt();
        this.others = new HashMap<>(othersSize);
        for (int i = 0; i < othersSize; i++) {
            String key = in.readString();
            Parcelable value = in.readParcelable(Parcelable.class.getClassLoader());
            this.others.put(key, value);
        }
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
