package com.ciandt.dragonfly.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;

public class Model implements Parcelable {

    public static final String SEPARATOR = ",";

    private String id = "";
    private int version = 0;

    private String modelPath = "";
    private String labelFilesPaths = "";

    private long sizeInBytes = 0;

    private int inputSize = 0;
    private int imageMean = 0;
    private float imageStd = 0f;
    private String inputName = "";
    private String outputNames = "";
    private String outputDisplayNames = "";
    private String closedSet = "";

    private HashMap<String, Serializable> others = new HashMap<>();


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

    public String[] getLabelFilesPaths() {
        return labelFilesPaths.split(SEPARATOR);
    }

    public Model setLabelFilesPaths(String labelFilesPaths) {
        this.labelFilesPaths = labelFilesPaths;
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

    public String[] getOutputNames() {
        return outputNames.split(SEPARATOR);
    }

    public Model setOutputNames(String outputNames) {
        this.outputNames = outputNames;
        return this;
    }

    public String[] getOutputDisplayNames() {
        return outputDisplayNames.split(SEPARATOR);
    }

    public Model setOutputDisplayNames(String outputDisplayNames) {
        this.outputDisplayNames = outputDisplayNames;
        return this;
    }

    public String[] getClosedSet() {
        return closedSet.split(SEPARATOR);
    }

    public Model setClosedSet(String closedSet) {
        this.closedSet = closedSet;
        return this;
    }

    public HashMap<String, Serializable> getOthers() {
        return others;
    }

    public Model setOthers(HashMap<String, Serializable> others) {
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
        if (labelFilesPaths != null ? !labelFilesPaths.equals(model.labelFilesPaths) : model.labelFilesPaths != null)
            return false;
        if (inputName != null ? !inputName.equals(model.inputName) : model.inputName != null)
            return false;
        if (outputNames != null ? !outputNames.equals(model.outputNames) : model.outputNames != null)
            return false;
        if (outputDisplayNames != null ? !outputDisplayNames.equals(model.outputDisplayNames) : model.outputDisplayNames != null)
            return false;
        if (closedSet != null ? !closedSet.equals(model.closedSet) : model.closedSet != null)
            return false;
        return others != null ? others.equals(model.others) : model.others == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (modelPath != null ? modelPath.hashCode() : 0);
        result = 31 * result + (labelFilesPaths != null ? labelFilesPaths.hashCode() : 0);
        result = 31 * result + Long.valueOf(sizeInBytes).hashCode();
        result = 31 * result + inputSize;
        result = 31 * result + imageMean;
        result = 31 * result + (imageStd != +0.0f ? Float.floatToIntBits(imageStd) : 0);
        result = 31 * result + (inputName != null ? inputName.hashCode() : 0);
        result = 31 * result + (outputNames != null ? outputNames.hashCode() : 0);
        result = 31 * result + (outputDisplayNames != null ? outputDisplayNames.hashCode() : 0);
        result = 31 * result + (closedSet != null ? closedSet.hashCode() : 0);
        result = 31 * result + (others != null ? others.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Model{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", modelPath='" + modelPath + '\'' +
                ", labelFilesPaths='" + labelFilesPaths + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                ", inputSize=" + inputSize +
                ", imageMean=" + imageMean +
                ", imageStd=" + imageStd +
                ", inputName='" + inputName + '\'' +
                ", outputNames='" + outputNames + '\'' +
                ", outputDisplayNames='" + outputDisplayNames + '\'' +
                ", closedSet='" + closedSet + '\'' +
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
        dest.writeString(this.labelFilesPaths);
        dest.writeLong(this.sizeInBytes);
        dest.writeInt(this.inputSize);
        dest.writeInt(this.imageMean);
        dest.writeFloat(this.imageStd);
        dest.writeString(this.inputName);
        dest.writeString(this.outputNames);
        dest.writeString(this.outputDisplayNames);
        dest.writeString(this.closedSet);
        dest.writeSerializable(this.others);
    }

    protected Model(Parcel in) {
        this.id = in.readString();
        this.version = in.readInt();
        this.modelPath = in.readString();
        this.labelFilesPaths = in.readString();
        this.sizeInBytes = in.readLong();
        this.inputSize = in.readInt();
        this.imageMean = in.readInt();
        this.imageStd = in.readFloat();
        this.inputName = in.readString();
        this.outputNames = in.readString();
        this.outputDisplayNames = in.readString();
        this.closedSet = in.readString();
        this.others = (HashMap<String, Serializable>) in.readSerializable();
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
