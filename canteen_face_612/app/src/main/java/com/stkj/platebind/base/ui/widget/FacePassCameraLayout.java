package com.stkj.platebind.base.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.stkj.platebind.MainApplication;
import com.stkj.platebind.R;
import com.stkj.common.core.AppManager;
import com.stkj.common.glide.GlideApp;
import com.stkj.common.log.LogHelper;
import com.stkj.common.ui.widget.surfaceview.AutoFitSurfaceView;

/**
 * 人脸识别摄像头布局
 */
public class FacePassCameraLayout extends FrameLayout {

    private AutoFitSurfaceView irPreviewFace;
    private AutoFitSurfaceView previewFace;
    private ImageView ivDefaultFace;
    private TextView tvFaceTips;
    private boolean isPreviewFace;

    public FacePassCameraLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public FacePassCameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FacePassCameraLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        int consumeLayRes = 0;
        if (attributeSet != null) {
            TypedArray array = context.obtainStyledAttributes(attributeSet, com.stkj.platebind.R.styleable.FacePassCameraLayout);
            isPreviewFace = array.getBoolean(R.styleable.FacePassCameraLayout_facePreview, false);
            consumeLayRes = array.getInteger(R.styleable.FacePassCameraLayout_fpcl_consume_lay_res, 0);
        }
        if (consumeLayRes == 1) {
            LayoutInflater.from(context).inflate(R.layout.include_face_pass_camera_s1, this);
        } else if (consumeLayRes == 2) {
            LayoutInflater.from(context).inflate(R.layout.include_face_pass_camera_s2, this);
        } else {
            LayoutInflater.from(context).inflate(R.layout.include_face_pass_camera, this);
        }
        previewFace = (AutoFitSurfaceView) findViewById(R.id.preview_face);
        previewFace.setAutoFitSurfaceListener(new AutoFitSurfaceView.OnAutoFitSurfaceListener() {
            @Override
            public void onMeasuredDimension(int width, int height) {
                if (height > 0 && width > height) {
                    int offset = -(width - height) / 2;
                    previewFace.setTranslationX(offset);
                    LogHelper.print("setAutoFitSurfaceListener---offset: " + offset);
                }
            }
        });
        irPreviewFace = (AutoFitSurfaceView) findViewById(R.id.ir_preview_face);
        irPreviewFace.setAutoFitSurfaceListener(new AutoFitSurfaceView.OnAutoFitSurfaceListener() {
            @Override
            public void onMeasuredDimension(int width, int height) {
                if (height > 0 && width > height) {
                    int offset = -(width - height) / 2;
                    irPreviewFace.setTranslationX(offset);
                    LogHelper.print("setAutoFitSurfaceListener---offset: " + offset);
                }
            }
        });
        ivDefaultFace = (ImageView) findViewById(R.id.iv_default_face);
        tvFaceTips = (TextView) findViewById(R.id.tv_face_tips);
        if (isPreviewFace) {
            setPreviewFace(true);
        }
    }

    public void setPreviewFace(boolean b) {
        isPreviewFace = b;
        if (isPreviewFace) {
            //TODO: 延迟显示摄像头预览
            if (MainApplication.isFirst) {
                previewFace.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainApplication.isFirst = false;
                            ivDefaultFace.setImageResource(0);
                    }
                }, 600);
            }else {
                    ivDefaultFace.setImageResource(0);
            }
        } else {
            ivDefaultFace.setImageResource(R.mipmap.icon_welcome_consumer);
        }
    }

    public void setFaceCameraTips(String tips) {
        if (tvFaceTips != null) {
            tvFaceTips.setText(tips);
            if (tips.contains("消费成功") || tips.contains("支付成功")){
                tvFaceTips.setTextColor(Color.parseColor("#0087FA"));
                tvFaceTips.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.mipmap.ic_pay_success), null, null, null);
            }else {
                tvFaceTips.setTextColor(Color.parseColor("#FF000000"));
                tvFaceTips.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        }
    }

    public void setFaceImage(String detectImage) {
        if (ivDefaultFace != null) {
            GlideApp.with(AppManager.INSTANCE.getApplication()).load(detectImage)
                    .circleCrop()
                    .placeholder(R.mipmap.icon_welcome_consumer)
                    .into(ivDefaultFace);
        }
    }

    public void resetFaceInfoLayout() {
        if (ivDefaultFace != null) {
            ivDefaultFace.setImageResource(R.mipmap.icon_welcome_consumer);
        }
        if (tvFaceTips != null) {
            tvFaceTips.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            tvFaceTips.setTextColor(Color.parseColor("#FF000000"));
            tvFaceTips.setText("欢迎光临!");
        }
    }

    public SurfaceView getFacePreviewFace() {
        return previewFace;
    }

    public AutoFitSurfaceView getIrPreviewFace() {
        return irPreviewFace;
    }
}
