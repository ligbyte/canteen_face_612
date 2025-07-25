package com.stkj.platebind.base.net;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.stkj.platebind.base.device.DeviceManager;
import com.stkj.platebind.base.utils.EncryptUtils;
import com.stkj.common.log.LogHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ParamsUtils {

    public static final String DES_PUBLIC_KEY = "St-tech@2023";

    /**
     * 新建请求排序的参数Map
     */
    private static TreeMap<String, String> newSortParamsMap() {
        return new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }

    /**
     * 新建请求排序的参数Map(包含设备参数信息和mode)
     */
    public static TreeMap<String, String> newSortParamsMapWithMode(String mode) {
        TreeMap<String, String> paramsMap = newSortParamsMap();
        paramsMap.put("machine_Number", DeviceManager.INSTANCE.getDeviceInterface().getMachineNumber());
        paramsMap.put("mode", mode);
        return paramsMap;
    }

    /**
     * 新建包含设备参数信息请求参数
     */
    public static Map<String, String> newMachineParamsMap() {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("machine_Number", DeviceManager.INSTANCE.getDeviceInterface().getMachineNumber());
        return paramsMap;
    }

    public static Map<String, String> signSortParamsMap(@NonNull TreeMap<String, String> newParamsMap) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : newParamsMap.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (!"sign".equals(key) && !"mode".equals(key) && !"consumption_type".equals(key) && !TextUtils.isEmpty(val)) {
                stringBuilder.append(val).append("&");
            }
        }
        String builderString = stringBuilder.toString();
        if (builderString.length() >= 2) {
            String signParamsValue = builderString.substring(0, builderString.length() - 1);
            LogHelper.print("signSortParamsMap sign = " + signParamsValue);
            newParamsMap.put("sign", EncryptUtils.encryptMD5ToString16(signParamsValue));
        }
        return newParamsMap;
    }
}
