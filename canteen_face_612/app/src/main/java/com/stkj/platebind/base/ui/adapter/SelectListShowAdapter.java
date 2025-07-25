package com.stkj.platebind.base.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.stkj.platebind.R;
import com.stkj.platebind.setting.model.FoodInfoTable;

public class SelectListShowAdapter extends BaseQuickAdapter<FoodInfoTable, BaseViewHolder> {

    public final static String TAG = "SelectListShowAdapter";
    private Context context;

    public SelectListShowAdapter(Context context)
    {
        super(R.layout.item_select_expand_list);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder holder, FoodInfoTable item) {
        holder.setText(R.id.tv_name, item.getName());
        Log.d(TAG, "limefoods SelectListShowAdapter : " + JSON.toJSONString(item));
        if (item.isSelected()) {
            holder.getView(R.id.iv_select).setVisibility(View.VISIBLE);
            holder.getView(R.id.item_root).setBackgroundColor(context.getResources().getColor(R.color.color_e6f7ff));

        } else {
            holder.getView(R.id.iv_select).setVisibility(View.GONE);
            holder.getView(R.id.item_root).setBackgroundColor(context.getResources().getColor(R.color.white));
        }

    }
}
