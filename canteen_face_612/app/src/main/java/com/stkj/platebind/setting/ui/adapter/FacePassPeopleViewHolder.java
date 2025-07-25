package com.stkj.platebind.setting.ui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.stkj.platebind.BuildConfig;
import com.stkj.platebind.R;
import com.stkj.platebind.base.ui.dialog.CommonAlertDialogFragment;
import com.stkj.platebind.base.utils.JacksonUtils;
import com.stkj.platebind.setting.model.FacePassPeopleInfo;
import com.stkj.common.glide.GlideApp;
import com.stkj.common.ui.adapter.holder.CommonRecyclerViewHolder;
import com.stkj.common.utils.StringUtils;

/**
 * 人脸库列表item
 */
public class FacePassPeopleViewHolder extends CommonRecyclerViewHolder<FacePassPeopleInfo> {

    private TextView tvName;
    private TextView tvAccount;
    private TextView tvAccountCategory;
    private TextView tvDepartment;
    private ImageView ivFace;
    private TextView tvUpdateTime;
    private TextView tvAddFaceStatus;

    public FacePassPeopleViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void initViews(View itemView) {
        tvName = (TextView) findViewById(R.id.tv_name);
        tvAccount = (TextView) findViewById(R.id.tv_account);
        tvAccountCategory = (TextView) findViewById(R.id.tv_account_category);
        tvDepartment = (TextView) findViewById(R.id.tv_department);
        ivFace = (ImageView) findViewById(R.id.iv_face);
        tvUpdateTime = (TextView) findViewById(R.id.tv_update_time);
        tvAddFaceStatus = (TextView) findViewById(R.id.tv_add_face_status);
        if (BuildConfig.DEBUG) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CommonAlertDialogFragment.build()
                            .setAlertTitleTxt("人员信息")
                            .setAlertContentTxt(JacksonUtils.convertJsonString(mData))
                            .show(mContext);
                    return true;
                }
            });
        }
    }

    @Override
    public void initData(FacePassPeopleInfo data) {
        tvName.setText(StringUtils.nameDesensitization(data.getFull_Name()));
        tvAccount.setText(StringUtils.getMixUpMobile(data.getPhone()));
        tvAccountCategory.setText(data.getAccountType());
        tvDepartment.setText(data.getDepNameType());
        tvUpdateTime.setText(data.getOpening_date());
        GlideApp.with(mContext)
                .load(data.getImgData())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.mipmap.icon_no_person_mini)
                .into(ivFace);
        /**
         * addFace = 0：成功  FaceResult = 20
         * addFace = 1：没有检测到人脸 FaceResult = 3
         * addFace= 2：检测到人脸，但是没有通过质量判断  FaceResult = 4
         */
        int cbgCheckFaceResult = data.getCBGCheckFaceResult();
        int statusNormalColor = mContext.getResources().getColor(R.color.color_171717);
        int statusErrorColor = mContext.getResources().getColor(R.color.color_FF3C30);
        tvAddFaceStatus.setTextColor(statusErrorColor);
        String addFaceStatus = "";
        if (cbgCheckFaceResult == 20) {
            tvAddFaceStatus.setTextColor(statusNormalColor);
            addFaceStatus = "入库成功";
        } else {
            addFaceStatus = "入库失败(" + cbgCheckFaceResult + ")";
        }
        tvAddFaceStatus.setText(addFaceStatus);
    }

    public static class Factory implements CommonRecyclerViewHolder.Factory<FacePassPeopleInfo> {
        @Override
        public CommonRecyclerViewHolder<FacePassPeopleInfo> createViewHolder(View itemView) {
            return new FacePassPeopleViewHolder(itemView);
        }

        @Override
        public int getLayResId() {
            return com.stkj.platebind.R.layout.item_facepass_people_info;
        }

        @Override
        public Class<FacePassPeopleInfo> getItemDataClass() {
            return FacePassPeopleInfo.class;
        }
    }
}