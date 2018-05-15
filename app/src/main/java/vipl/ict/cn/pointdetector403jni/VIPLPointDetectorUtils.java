package vipl.ict.cn.pointdetector403jni;

/**
 * Created by ict on 5/22/2017.
 */

public class VIPLPointDetectorUtils {
    static {
        System.loadLibrary("PointDetector403Jni");
    }
    public native int InitPointDetector(String mode_path);

    public native int[] PointDetect(byte[] argb, int width, int height, int num_faces, int[] face_info);

    public native boolean ReleasePointDetector();
}
