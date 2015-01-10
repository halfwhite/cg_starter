package com.google.android.hotword.client;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.hotword.service.IHotwordService;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class HotwordServiceClient {
    @SuppressWarnings("unused")
    private static final boolean DBG = false;
    private static final String HOTWORD_SERVICE = "com.google.android.googlequicksearchbox.HOTWORD_SERVICE";
    private static final String TAG = "HotwordServiceClient";
    private static final String VEL_PACKAGE = "com.google.android.googlequicksearchbox";

    private final Context mContext;
    private final ServiceConnection mConnection;

    private IHotwordService mHotwordService;

    private boolean mHotwordStart;
    private boolean mIsAvailable = true;
    private boolean mIsBound;
    private boolean mIsRequested = false;

    public HotwordServiceClient(Context context) {
        mContext = context;
        mConnection = new HotwordServiceConnection();
    }

    public boolean isAvailable() {
        internalBind();
        return mIsAvailable;
    }

    private void assertMainThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalStateException("Must be called on the main thread.");
    }

    private void internalBind() {
        if (!mIsAvailable || mIsBound) {
            if (!mIsAvailable)
                Log.w(TAG, "Hotword service is not available.");
            return;
        }

        Intent localIntent = new Intent(HOTWORD_SERVICE).setPackage(VEL_PACKAGE);
        mIsAvailable = mContext.bindService(localIntent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = mIsAvailable;
    }

    private void internalRequestHotword() {
        if (mIsRequested) {
            if (!mHotwordStart) {
                mHotwordStart = true;
                if (!mIsBound) {
                    internalBind();
                }
            }
        }

        try {
            if (mHotwordService != null)
                mHotwordService.requestHotwordDetection(mContext.getPackageName(), mIsRequested);
        } catch (RemoteException e) {
            Log.w(TAG, "requestHotwordDetection - remote call failed", e);
            return;
        }
    }

    public final void requestHotwordDetection(boolean detect) {
        if (detect == mIsRequested)
            return;

        assertMainThread();
        mIsRequested = detect;
        internalRequestHotword();
    }

    private class HotwordServiceConnection implements ServiceConnection {
        private HotwordServiceConnection() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mHotwordService = IHotwordService.Stub.asInterface(iBinder);
            internalRequestHotword();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mIsBound = false;
            mHotwordService = null;
        }
    }

}