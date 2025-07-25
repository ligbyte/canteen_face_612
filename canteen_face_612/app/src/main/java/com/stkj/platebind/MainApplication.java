package com.stkj.platebind;

import android.app.Application;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.stkj.platebind.base.device.DeviceManager;
import com.stkj.platebind.base.glide.GlideAppHelper;
import com.stkj.platebind.base.net.AppNetManager;
import com.stkj.platebind.machine.utils.LedCtrlUtil;
import com.stkj.platebind.machine.utils.SharePreUtil;
import com.stkj.platebind.pay.model.ConsumeFoodInfo;
import com.stkj.platebind.pay.model.FoodConsumeDetailResponse;
import com.stkj.platebind.setting.data.DeviceSettingMMKV;
import com.stkj.platebind.setting.data.ServerSettingMMKV;
import com.stkj.common.constants.AppCommonConstants;
import com.stkj.common.core.AppManager;
import com.stkj.common.crash.XCrashHelper;
import com.stkj.common.log.LogHelper;
import com.stkj.common.mmkv.MMKVInit;
import com.stkj.common.net.okhttp.OkHttpManager;
import com.stkj.common.net.retrofit.RetrofitManager;
import com.stkj.common.rx.RxHelper;
import com.stkj.common.utils.ProcessUtils;

import java.util.List;

public class MainApplication extends Application {

    public static String createOrderNumber = "";
    public static MainApplication instances;
    public static boolean isUnLockFrame = true;
    public static boolean isNeedCache = false;
    public static boolean isJuHePay = false;
    public static int isQuick = 0;
    public static int consumptionType = 0;
    public static List<ConsumeFoodInfo> consumeFoodInfoList;
    public static FoodConsumeDetailResponse foodConsumeDetailResponse;
    public static int width = 1920;
    public static int height = 1080;
    public static String barcode = "882766";
    public static boolean isFirst = true;
    public static boolean hasWeight = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instances = this;
        Log.i("MainApplication", "----onCreate pid: " + Process.myPid());
        SharePreUtil.getInstance();
        LedCtrlUtil.getInstance();
        AppCommonConstants.APP_PREFIX_TAG = "canteen";
        AppManager.INSTANCE.init(this);
        if (!ProcessUtils.isMainProcess()) {
            return;
        }
        MMKVInit.init(this);
        RxHelper.init();
        OkHttpManager.INSTANCE.setHttpLogEnable(BuildConfig.DEBUG);
        OkHttpManager.INSTANCE.setLogSwitch(BuildConfig.DEBUG);
        OkHttpManager.INSTANCE.setDefIntercept(AppNetManager.INSTANCE.getAppOkhttpHttpIntercept());
        RetrofitManager.INSTANCE.setConvertJsonListener(AppNetManager.INSTANCE.getRetrofitJsonConvertListener());
        if (BuildConfig.DEBUG) {
            String serverAddress = ServerSettingMMKV.getServerAddress();
            if (!TextUtils.isEmpty(serverAddress)) {
                RetrofitManager.INSTANCE.setDefaultBaseUrl(serverAddress);
            } else {
                RetrofitManager.INSTANCE.setDefaultBaseUrl(AppNetManager.API_TEST_URL);
            }
        } else {
            RetrofitManager.INSTANCE.setDefaultBaseUrl(AppNetManager.API_OFFICIAL_URL);
        }
        //初始化Glide
        GlideAppHelper.init(this);
        //初始化设备
        DeviceManager.INSTANCE.initDevice(this);
//        if (!AppCommonMMKV.getHasAddShutCut()) {
//            ShutCutIconUtils.addShortCutCompact(this);
//            AppCommonMMKV.putHasAddShutCut(true);
//        }
        //崩溃日志
        XCrashHelper.init(this);
        //普通日志
        String machineNumber = DeviceManager.INSTANCE.getDeviceInterface().getMachineNumber();
        if (TextUtils.isEmpty(machineNumber)) {
            LogHelper.init();
        } else {
            LogHelper.init(machineNumber + "_log_");
        }
        boolean openSysLog = DeviceSettingMMKV.isOpenSysLog();
        LogHelper.setLogEnable(openSysLog || BuildConfig.DEBUG);
        LogHelper.print("---MainApplication--getMachineNumber: " + machineNumber);
    }
}
