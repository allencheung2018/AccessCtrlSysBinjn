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

public abstract class Callback_FaceAlgSvr_GetFaceCount
    extends IceInternal.TwowayCallback implements Ice.TwowayCallbackInt
{
    public final void __completed(Ice.AsyncResult __result)
    {
        FaceAlgSvrPrxHelper.__GetFaceCount_completed(this, __result);
    }
}
