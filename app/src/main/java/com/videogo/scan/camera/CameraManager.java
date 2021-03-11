/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.videogo.scan.camera;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;

import com.videogo.scan.main.PlanarYUVLuminanceSource;
import com.videogo.scan.main.PreferencesActivity;
import com.videogo.util.LogUtil;

public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    // private static final int MIN_FRAME_WIDTH = 240;
    // private static final int MIN_FRAME_HEIGHT = 240;
    // private static final int MAX_FRAME_WIDTH = 600;
    // private static final int MAX_FRAME_HEIGHT = 400;

    private final Context context;

    private final CameraConfigurationManager configManager;

    private Camera camera;

    private Rect framingRect;

    private Rect framingRectInPreview;

    private boolean initialized;

    private boolean previewing;

    private boolean reverseImage;

    private int requestedFramingRectWidth;

    private int requestedFramingRectHeight;

    private final PreviewCallback previewCallback;

    private final AutoFocusCallback autoFocusCallback;

    public CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        previewCallback = new PreviewCallback(configManager);
        autoFocusCallback = new AutoFocusCallback();
    }

    public void openDriver(SurfaceHolder holder) throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = Camera.open();
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
        }
        theCamera.setPreviewDisplay(holder);

        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(theCamera);
            if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
                setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight);
                requestedFramingRectWidth = 0;
                requestedFramingRectHeight = 0;
            }
        }
        configManager.setDesiredCameraParameters(theCamera);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        reverseImage = prefs.getBoolean(PreferencesActivity.KEY_REVERSE_IMAGE, false);
    }

    public void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    public void startPreview() {
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
        }
    }

    public void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    public void requestPreviewFrame(Handler handler, int message) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, message);
            theCamera.setOneShotPreviewCallback(previewCallback);
        }
    }

    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message);
            try {
                camera.autoFocus(autoFocusCallback);
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                LogUtil.w(TAG, "Unexpected exception while focusing", re);
            }
        }
    }

    public Rect getFramingRect() {
        // if (framingRect == null) {
        // if (camera == null) {
        // return null;
        // }
        // Point screenResolution = configManager.getScreenResolution();
        // int width = screenResolution.x * 3 / 4;
        // if (width < MIN_FRAME_WIDTH) {
        // width = MIN_FRAME_WIDTH;
        // } else if (width > MAX_FRAME_WIDTH) {
        // width = MAX_FRAME_WIDTH;
        // }
        // int height = screenResolution.y * 3 / 4;
        // if (height < MIN_FRAME_HEIGHT) {
        // height = MIN_FRAME_HEIGHT;
        // } else if (height > MAX_FRAME_HEIGHT) {
        // height = MAX_FRAME_HEIGHT;
        // }
        // int leftOffset = (screenResolution.x - width) / 2;
        // int topOffset = (screenResolution.y - height) / 2;
        // framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        // LogUtil.d(TAG, "Calculated framing rect: " + framingRect);
        // }
        // return framingRect;

        Point screenResolution = configManager.getScreenResolution();
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }

            int height;
            int width = (int) (screenResolution.y * 0.83);
            height = width;
            int leftOffset = (screenResolution.y - width) / 2;
            int topOffset = (screenResolution.x - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            LogUtil.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            rect.left = rect.left * cameraResolution.y / screenResolution.y;
            rect.right = rect.right * cameraResolution.y / screenResolution.y;
            rect.top = rect.top * cameraResolution.x / screenResolution.x;
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.x;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    public void setManualFramingRect(int width, int height) {
        if (initialized) {
            Point screenResolution = configManager.getScreenResolution();
            if (width > screenResolution.x) {
                width = screenResolution.x;
            }
            if (height > screenResolution.y) {
                height = screenResolution.y;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            LogUtil.d(TAG, "Calculated manual framing rect: " + framingRect);
            framingRectInPreview = null;
        } else {
            requestedFramingRectWidth = width;
            requestedFramingRectHeight = height;
        }
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(),
                reverseImage);
    }

}
