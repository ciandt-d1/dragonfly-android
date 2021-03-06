package com.ciandt.dragonfly.tensorflow;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Created by amitshekhar on 06/03/17.
 */

/**
 * A classifier specialized to label images using TensorFlow.
 */
public class TensorFlowImageClassifier implements Classifier {

    private static final String LOG_TAG = TensorFlowImageClassifier.class.getSimpleName();
    private static final String ASSET_FILE_PREFIX = "file:///android_asset/";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 5;
    private static final float THRESHOLD = 0.01f;

    // Config values.
    private String inputName;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    // Pre-allocated buffers.
    private final Map<String, Vector<String>> labels = new HashMap<>();
    private final Map<String, Integer> numClasses = new LinkedHashMap<>();
    private int[] intValues;
    private float[] floatValues;
    private String[] outputNames;

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowImageClassifier() {
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager   The asset manager to be used to load assets.
     * @param modelFilename  The filepath of the model GraphDef protocol buffer.
     * @param labelFilenames The filepaths of label files for classes.
     * @param inputSize      The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean      The assumed mean of the image values.
     * @param imageStd       The assumed std of the image values.
     * @param inputName      The label of the image input node.
     * @param outputNames    The labels of the outputs node.
     */
    public static TensorFlowImageClassifier create(
            AssetManager assetManager,
            String modelFilename,
            String[] labelFilenames,
            int inputSize,
            int imageMean,
            float imageStd,
            String inputName,
            String[] outputNames)
            throws IOException {
        TensorFlowImageClassifier c = new TensorFlowImageClassifier();
        c.inputName = inputName;

        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        for (int i = 0; i < outputNames.length; i++) {
            String outputName = outputNames[i];
            String labelFilename = labelFilenames[i];

            // Loading labels
            final boolean hasAssetPrefix = labelFilename.startsWith(ASSET_FILE_PREFIX);
            InputStream is;
            String actualLabelFilename = null;

            try {
                actualLabelFilename = hasAssetPrefix ? labelFilename.split(ASSET_FILE_PREFIX)[1] : labelFilename;
                is = assetManager.open(actualLabelFilename);
            } catch (IOException assetIOException) {
                if (hasAssetPrefix) {
                    throw new RuntimeException("Failed to load model from '" + labelFilename + "'", assetIOException);
                }
                // Perhaps the model file is not an asset but is on disk.
                try {
                    is = new FileInputStream(labelFilename);
                } catch (IOException externalFileIOException) {
                    throw new RuntimeException("Failed to load model from '" + labelFilename + "'", externalFileIOException);
                }
            }

            Log.i(LOG_TAG, "Reading labels from: " + actualLabelFilename);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            Vector<String> labels = new Vector<>();
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
            c.labels.put(outputName, labels);

            // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
            int numClasses = (int) c.inferenceInterface.graph().operation(outputName).output(0).shape().size(1);

            Log.i(LOG_TAG, "Read " + labels.size() + " labels, output layer size is " + numClasses);
            c.numClasses.put(outputName, numClasses);
        }

        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
        // must be passed in as a parameter.
        c.inputSize = inputSize;
        c.imageMean = imageMean;
        c.imageStd = imageStd;

        // Pre-allocate buffers.
        c.outputNames = outputNames;
        c.intValues = new int[inputSize * inputSize];
        c.floatValues = new float[inputSize * inputSize * 3];

        return c;
    }

    public Map<String, List<Classification>> classifyImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("classifyImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
            floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
        }
        Trace.endSection();

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames);
        Trace.endSection();


        final LinkedHashMap<String, List<Classification>> multiClassifications = new LinkedHashMap<>();

        for (String outputName : outputNames) {
            float[] outputs = new float[numClasses.get(outputName)];

            // Copy the output Tensor back into the output array.
            Trace.beginSection("fetch");
            inferenceInterface.fetch(outputName, outputs);
            Trace.endSection();

            // Find the best classifications.
            PriorityQueue<Classification> pq =
                    new PriorityQueue<>(
                            3,
                            new Comparator<Classification>() {

                                @Override
                                public int compare(Classification lhs, Classification rhs) {
                                    // Intentionally reversed to put high confidence at the head of the queue.
                                    return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                                }
                            });

            for (int i = 0; i < outputs.length; ++i) {
                if (outputs[i] > THRESHOLD) {
                    pq.add(
                            new Classification(
                                    Integer.toString(i), labels.get(outputName).size() > i ? labels.get(outputName).get(i) : "unknown", outputs[i], null
                            )
                    );
                }
            }

            final ArrayList<Classification> classifications = new ArrayList<>();
            int classificationsSize = Math.min(pq.size(), MAX_RESULTS);
            for (int i = 0; i < classificationsSize; ++i) {
                classifications.add(pq.poll());
            }

            multiClassifications.put(outputName, classifications);
        }

        Trace.endSection(); // "classifyImage"
        return multiClassifications;
    }

    public void enableStatLogging(boolean debug) {
//        inferenceInterface. enableStatLogging(debug);
    }

    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    public void close() {
        inferenceInterface.close();
    }
}
