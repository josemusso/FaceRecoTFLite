package com.zwp.mobilefacenet.mobilefacenet;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.zwp.mobilefacenet.MyUtil;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.util.Arrays;

/**
 * 人脸比对
 */
public class MobileFaceNet {
    private static final String MODEL_FILE = "MobileFaceNet.tflite";

    public static final int INPUT_IMAGE_SIZE = 112; // The width and height of the placeholder image that needs feed data
    public static final float THRESHOLD = 0.8f; // Set a threshold value greater than this value to consider the same person

    private Interpreter interpreter;

    public MobileFaceNet(AssetManager assetManager) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(MyUtil.loadModelFile(assetManager, MODEL_FILE), options);
    }

    public float[][] getEmedding(Bitmap bitmap1, Bitmap bitmap2) {
        // Resize the face to 112X112, because the shape of the placeholder that needs feed data is (2, 112, 112, 3)
        Bitmap bitmapScale1 = Bitmap.createScaledBitmap(bitmap1, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true);
        Bitmap bitmapScale2 = Bitmap.createScaledBitmap(bitmap2, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true);

        // create dataset structure to pass through the network
        float[][][][] datasets = getTwoImageDatasets(bitmapScale1, bitmapScale2);
        float[][] embeddings = new float[2][192];
        interpreter.run(datasets, embeddings);
        MyUtil.l2Normalize(embeddings, 1e-10);
        //Log.d("EMBEDDINGS", Arrays.toString(embeddings[0]));
        //Log.d("EMBEDDINGS", Arrays.toString(embeddings[1]));
        return embeddings;
    }

    public float compare(Bitmap bitmap1, Bitmap bitmap2) {
        // Resize the face to 112X112, because the shape of the placeholder that needs feed data is (2, 112, 112, 3)
        Bitmap bitmapScale1 = Bitmap.createScaledBitmap(bitmap1, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true);
        Bitmap bitmapScale2 = Bitmap.createScaledBitmap(bitmap2, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true);

        // create dataset structure to pass through the network
        float[][][][] datasets = getTwoImageDatasets(bitmapScale1, bitmapScale2);
        float[][] embeddings = new float[2][192];
        interpreter.run(datasets, embeddings);
        MyUtil.l2Normalize(embeddings, 1e-10);
        //Log.d("EMBEDDINGS", Arrays.toString(embeddings[0]));
        //Log.d("EMBEDDINGS", Arrays.toString(embeddings[1]));
        return evaluate(embeddings);
    }

    /**
     * Calculate the similarity of two pictures, using l2 loss
     * @param embeddings
     * @return
     */
    private float evaluate(float[][] embeddings) {
        float[] embeddings1 = embeddings[0];
        float[] embeddings2 = embeddings[1];
        float dist = 0;
        for (int i = 0; i < 192; i++) {
            dist += Math.pow(embeddings1[i] - embeddings2[i], 2);
        }
        float same = 0;
        for (int i = 0; i < 400; i++) {
            float threshold = 0.01f * (i + 1);
            if (dist < threshold) {
                same += 1.0 / 400;
            }
        }
        return same;
    }

    /**
     * 转换两张图片为归一化后的数据
     * @param bitmap1
     * @param bitmap2
     * @return
     */
    private float[][][][] getTwoImageDatasets(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap[] bitmaps = {bitmap1, bitmap2};

        int[] ddims = {bitmaps.length, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, 3};
        float[][][][] datasets = new float[ddims[0]][ddims[1]][ddims[2]][ddims[3]];

        for (int i = 0; i < ddims[0]; i++) {
            Bitmap bitmap = bitmaps[i];
            datasets[i] = MyUtil.normalizeImage(bitmap);
        }
        return datasets;
    }
}
