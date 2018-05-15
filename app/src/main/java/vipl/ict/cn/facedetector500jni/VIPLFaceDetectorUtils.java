package vipl.ict.cn.facedetector500jni;

/**
 * Created by ict on 5/22/2017.
 */

public class VIPLFaceDetectorUtils {
    static {
        System.loadLibrary("FaceDetector500Jni");
    }

    public native int InitFaceDetector(String modelPath);
    public native boolean SetMinFace(int minFace);
    public native boolean SetImagePyramidScaleFactor(float scale_factor);
    public native boolean SetScoreThresh(float score_thresh1, float score_thresh2, float score_thresh3);
    public native int[] FaceDetect(byte[] argb, int width, int height, int[] num_faces);
    public native boolean ReleaseFaceDetector();
}
