package com.stkj.cashier.machine.bind;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.stkj.cashier.R;
import com.stkj.cashier.base.device.DeviceManager;
import com.stkj.cashier.base.ui.dialog.CommonBindAlertDialogFragment;
import com.stkj.cashier.base.utils.CommonDialogUtils;
import com.stkj.cashier.home.ui.activity.MainActivity;
import com.stkj.cashier.machine.adapter.SettingBindTabInfoViewHolder;
import com.stkj.cashier.machine.model.SettingBindTabInfo;
import com.stkj.cashier.pay.model.BindFragmentSwitchEvent;
import com.stkj.cashier.pay.ui.weight.GridSpacingItemDecoration;
import com.stkj.cashier.setting.model.SettingTabInfo;
import com.stkj.cashier.setting.ui.adapter.SettingTabInfoViewHolder;
import com.stkj.cashier.setting.ui.adapter.SettingTabPageAdapter;
import com.stkj.common.log.LogHelper;
import com.stkj.common.ui.adapter.CommonRecyclerAdapter;
import com.stkj.common.ui.fragment.BaseRecyclerFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置页面
 */
public class TabBindSettingFragment extends BaseRecyclerFragment {

    public final static String TAG = "TabBindSettingFragment";
    private RecyclerView rvTopTab;
    private ImageView iv_back;
    private LinearLayout ll_app_settings;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_bind_setting;
    }

    @Override
    protected void initViews(View rootView) {
        rvTopTab = (RecyclerView) findViewById(R.id.rv_top_tab);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        ll_app_settings = (LinearLayout) findViewById(R.id.ll_app_settings);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new BindFragmentSwitchEvent(0));
            }
        });
    }

    @Override
    protected void onFragmentResume(boolean isFirstOnResume) {
        if (isFirstOnResume) {
            initData();
        }
    }

    private void initData() {
        Log.d(TAG, "limeTabBindSettingFragment: " + 48);
        //添加设置tab
        List<SettingBindTabInfo> settingTabInfoList = new ArrayList<>();
        settingTabInfoList.add(new SettingBindTabInfo(SettingBindTabInfo.TAB_NAME_SERVER_ADDRESS, SettingBindTabInfo.TAB_TYPE_SERVER_ADDRESS,R.mipmap.ic_bind_settings_face));
        settingTabInfoList.add(new SettingBindTabInfo(SettingBindTabInfo.TAB_NAME_DEVICE_SETTING, SettingBindTabInfo.TAB_TYPE_DEVICE_SETTING,R.mipmap.ic_bind_settings_id));
        settingTabInfoList.add(new SettingBindTabInfo(SettingBindTabInfo.TAB_NAME_WIFI_CONNECT, SettingBindTabInfo.TAB_TYPE_WIFI_CONNECT,R.mipmap.ic_bind_settings));
        settingTabInfoList.add(new SettingBindTabInfo(SettingBindTabInfo.TAB_NAME_PAYMENT_SETTING, SettingBindTabInfo.TAB_TYPE_PAYMENT_SETTING,R.mipmap.ic_bind_settings_canting));
        CommonRecyclerAdapter tabInfoAdapter = new CommonRecyclerAdapter(false);
        tabInfoAdapter.addViewHolderFactory(new SettingBindTabInfoViewHolder.Factory());

        tabInfoAdapter.addDataList(settingTabInfoList);
        rvTopTab.setAdapter(tabInfoAdapter);
        rvTopTab.setItemAnimator(null);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), settingTabInfoList.size());
        rvTopTab.setLayoutManager(layoutManager);
        rvTopTab.addItemDecoration(new GridSpacingItemDecoration(settingTabInfoList.size(), 40, false));

        tabInfoAdapter.addItemEventListener(new CommonRecyclerAdapter.OnItemEventListener() {
            @Override
            public void onClickItemView(View view, Object obj) {
                SettingBindTabInfo settingTabInfo = (SettingBindTabInfo) obj;

                if (settingTabInfo.getTabName().equals(SettingBindTabInfo.TAB_NAME_DEVICE_SETTING)){
                    CommonDialogUtils.showTipsBindDialog(getActivity(), "设备ID",DeviceManager.INSTANCE.getDeviceInterface().getMachineNumber(), "取消", new CommonBindAlertDialogFragment.OnSweetClickListener() {
                        @Override
                        public void onClick(CommonBindAlertDialogFragment alertDialogFragment) {

                        }
                    });
                } else if (settingTabInfo.getTabName().equals(SettingBindTabInfo.TAB_NAME_PAYMENT_SETTING)){
                    CommonDialogUtils.showTipsBindDialog(getActivity(), "用餐设置","敬请期待", "取消", new CommonBindAlertDialogFragment.OnSweetClickListener() {
                        @Override
                        public void onClick(CommonBindAlertDialogFragment alertDialogFragment) {

                        }
                    });
                }else if (settingTabInfo.getTabName().equals(SettingBindTabInfo.TAB_NAME_WIFI_CONNECT)){
                    ll_app_settings.setVisibility(View.VISIBLE);
                    rvTopTab.setVisibility(View.GONE);
                }

            }
        });


    }
}
