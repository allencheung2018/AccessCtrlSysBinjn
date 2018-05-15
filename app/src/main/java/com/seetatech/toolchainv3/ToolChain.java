package com.seetatech.toolchainv3;

/**
 * Created by ict on 11/24/2017.
 */

public class ToolChain {
    static {
        System.loadLibrary("ToolChainNIRJni");
    }

    public native void Initialize(String modelDir, String faceDetectorModel, String pointDetectorModel,
                                  String faceRecognizerModel);

    public native void Destroy();

    public native void SetScoreThresh(double[] scoreThresh);

    public native void SetMinFaceSize(int minFaceSize);

    public native void SetImagePyramidScaleFactor(double imagePyramidScaleFactor);

    public native int[] FaceDetect(byte[] img, int width, int height);

    public native int GetLandmarkNum();

    public native int[] DetectLandmarks(byte[] img, int width, int height);

    public native int GetFeatureSize();

    public native boolean ExtractFeature(byte[] img, int width, int height, float[] feats);

    public native float CalcSimilarityWithTwoImages(byte[] img1, int width1, int height1,
                                                    byte[] img2,  int width2, int height2);

    public native float CalcSimilarity(float[] faceFeatures1, float[] faceFeatures2);

}
