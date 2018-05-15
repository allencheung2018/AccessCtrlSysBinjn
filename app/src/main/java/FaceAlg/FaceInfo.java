// **********************************************************************
//
// Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.6.4
//
// <auto-generated>
//
// Generated from file `FaceAlg.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package FaceAlg;

public class FaceInfo implements java.lang.Cloneable, java.io.Serializable
{
    public int x;

    public int y;

    public int width;

    public int height;

    public float roll;

    public float pitch;

    public float yaw;

    public float quality;

    public FPoint[] landmarks;

    public float[] features;

    public FaceInfo()
    {
    }

    public FaceInfo(int x, int y, int width, int height, float roll, float pitch, float yaw, float quality, FPoint[] landmarks, float[] features)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.quality = quality;
        this.landmarks = landmarks;
        this.features = features;
    }

    public boolean
    equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        FaceInfo _r = null;
        if(rhs instanceof FaceInfo)
        {
            _r = (FaceInfo)rhs;
        }

        if(_r != null)
        {
            if(x != _r.x)
            {
                return false;
            }
            if(y != _r.y)
            {
                return false;
            }
            if(width != _r.width)
            {
                return false;
            }
            if(height != _r.height)
            {
                return false;
            }
            if(roll != _r.roll)
            {
                return false;
            }
            if(pitch != _r.pitch)
            {
                return false;
            }
            if(yaw != _r.yaw)
            {
                return false;
            }
            if(quality != _r.quality)
            {
                return false;
            }
            if(!java.util.Arrays.equals(landmarks, _r.landmarks))
            {
                return false;
            }
            if(!java.util.Arrays.equals(features, _r.features))
            {
                return false;
            }

            return true;
        }

        return false;
    }

    public int
    hashCode()
    {
        int __h = 5381;
        __h = IceInternal.HashUtil.hashAdd(__h, "::FaceAlg::FaceInfo");
        __h = IceInternal.HashUtil.hashAdd(__h, x);
        __h = IceInternal.HashUtil.hashAdd(__h, y);
        __h = IceInternal.HashUtil.hashAdd(__h, width);
        __h = IceInternal.HashUtil.hashAdd(__h, height);
        __h = IceInternal.HashUtil.hashAdd(__h, roll);
        __h = IceInternal.HashUtil.hashAdd(__h, pitch);
        __h = IceInternal.HashUtil.hashAdd(__h, yaw);
        __h = IceInternal.HashUtil.hashAdd(__h, quality);
        __h = IceInternal.HashUtil.hashAdd(__h, landmarks);
        __h = IceInternal.HashUtil.hashAdd(__h, features);
        return __h;
    }

    public FaceInfo
    clone()
    {
        FaceInfo c = null;
        try
        {
            c = (FaceInfo)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void
    __write(IceInternal.BasicStream __os)
    {
        __os.writeInt(x);
        __os.writeInt(y);
        __os.writeInt(width);
        __os.writeInt(height);
        __os.writeFloat(roll);
        __os.writeFloat(pitch);
        __os.writeFloat(yaw);
        __os.writeFloat(quality);
        PointSeqHelper.write(__os, landmarks);
        FloatSeqHelper.write(__os, features);
    }

    public void
    __read(IceInternal.BasicStream __is)
    {
        x = __is.readInt();
        y = __is.readInt();
        width = __is.readInt();
        height = __is.readInt();
        roll = __is.readFloat();
        pitch = __is.readFloat();
        yaw = __is.readFloat();
        quality = __is.readFloat();
        landmarks = PointSeqHelper.read(__is);
        features = FloatSeqHelper.read(__is);
    }

    static public void
    __write(IceInternal.BasicStream __os, FaceInfo __v)
    {
        if(__v == null)
        {
            __nullMarshalValue.__write(__os);
        }
        else
        {
            __v.__write(__os);
        }
    }

    static public FaceInfo
    __read(IceInternal.BasicStream __is, FaceInfo __v)
    {
        if(__v == null)
        {
             __v = new FaceInfo();
        }
        __v.__read(__is);
        return __v;
    }
    
    private static final FaceInfo __nullMarshalValue = new FaceInfo();

    public static final long serialVersionUID = -1247978456L;
}
