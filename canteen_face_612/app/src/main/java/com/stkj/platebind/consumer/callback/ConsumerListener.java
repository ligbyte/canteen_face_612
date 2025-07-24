package com.stkj.platebind.consumer.callback;

import android.view.SurfaceView;

import com.stkj.platebind.home.ui.widget.HomeTitleLayout;

public interface ConsumerListener {

    default void onCreateFacePreviewView(SurfaceView previewView, SurfaceView irPreviewView) {

    }

    default void onCreateTitleLayout(HomeTitleLayout homeTitleLayout) {

    }

    default void onConsumerDismiss() {

    }

    default void onConsumerChanged() {

    }
}
