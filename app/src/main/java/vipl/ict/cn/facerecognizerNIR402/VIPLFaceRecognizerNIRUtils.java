package vipl.ict.cn.facerecognizerNIR402;

/**
 * Created by ict on 5/22/2017.
 */

public class VIPLFaceRecognizerNIRUtils {
    static {
        System.loadLibrary("native-lib");
    }
    public native int InitFaceRecognizer(String var1);

    public native boolean ReleaseFaceRecognizer();

    public native int  GetFeatureSize();

    public native int ExtractFeatures(long var1, float[] var4, float[] var5);

    public native float CalcSimilarity(float[] var1, float[] var2);
}
