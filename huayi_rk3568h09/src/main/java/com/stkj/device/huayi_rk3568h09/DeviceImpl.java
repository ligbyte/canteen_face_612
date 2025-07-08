package com.stkj.device.huayi_rk3568h09;

import static com.huayi.usbcard.CardAPI.BytetoHex;
import static com.huayi.usbcard.ReadCardApi.TYPE_CARD_INIT_FAIL;
import static com.huayi.usbcard.ReadCardApi.TYPE_CARD_INIT_OK;
import static com.huayi.usbcard.ReadCardApi.TYPE_FIND_CARD_SUCCESS;
import static com.huayi.usbcard.ReadCardApi.TYPE_UNKNOW_ERROR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.huayi.rk3568hyznjar.hyznjar.CustomAPI;
import com.huayi.usbcard.ReadCardApi;
import com.huayi.usbcard.ReadCardListener;
import com.stkj.common.log.LogHelper;
import com.stkj.common.ui.toast.AppToast;
import com.stkj.deviceinterface.DeviceInterface;
import com.stkj.deviceinterface.ScanKeyManager;
import com.stkj.deviceinterface.callback.OnReadICCardListener;
import com.stkj.deviceinterface.callback.OnScanQRCodeListener;
import com.stkj.deviceinterface.model.DeviceHardwareInfo;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.schedulers.Schedulers;
import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

/**
 * 实现天波D2板子
 */
public class DeviceImpl extends DeviceInterface implements ScanKeyManager.OnScanValueListener {

    public final static String TAG = "DeviceImpl";
    private ReadCardApi usbcard;
    //扫码串口
    private SerialHelper qrcodeSerialHelper;
    //扫码枪串口
    private SerialHelper scanGunSerialHelper;
    private Context context;
    private List<DeviceHardwareInfo> mUSBHardwareInfoList = new ArrayList<>();
    //键盘模式扫码枪
    private DeviceHardwareInfo keyBoardScanGunDeviceInfo = new DeviceHardwareInfo(7851, 6690, "扫码枪-键盘模式", DeviceHardwareInfo.TYPE_SCAN_GUN_KEYBOARD);
    //串口模式扫码枪
    private DeviceHardwareInfo serialScanGunDeviceInfo = new DeviceHardwareInfo(7851, 6662, "扫码枪-串口模式", DeviceHardwareInfo.TYPE_SCAN_GUN_SERIAL_PORT);
    private ScanKeyManager scanKeyManager;

    @Override
    public void init(Context context) {
        this.context = context;
        scanKeyManager = new ScanKeyManager(this);
        mUSBHardwareInfoList.add(keyBoardScanGunDeviceInfo);
        mUSBHardwareInfoList.add(serialScanGunDeviceInfo);
        ReadCardListener rcl = new ReadCardListener() {
            @Override
            public void onReadCardStateChanged(int type, int state, String msg) {
                switch (type) {
                    case TYPE_CARD_INIT_OK:
                        usbcard.cardBeep(200);
                        byte[] ver = new byte[5];
                        int ret = usbcard.get_firmware_version(ver);
                        int year = 2000 + ver[0];
                        int mon = ver[1];
                        int day = ver[2];
                        int hour = ver[3];
                        int min = ver[4];
                        if (ret == 1) {
                            //hy_beep(500);
                        } else {
                        }
                        break;
                    case TYPE_CARD_INIT_FAIL:
                        break;
                    case TYPE_FIND_CARD_SUCCESS:
                        byte[] sak = new byte[1];
                        sak[0] = (byte) state;
                        if (sak[0] == 0x20) {
                        } else if (sak[0] != 0) {
                            String realCardNumber = ParseData.decodeHexString(msg.replaceAll(" ",""));
                            if (!TextUtils.isEmpty(realCardNumber)) {
                                notifyOnReadCardData(realCardNumber);
                            }
                            readM1Card();

                        } else {
                            usbcard.cardBeep(200);
                        }

                        usbcard.closeCard();

                        break;
                    case TYPE_UNKNOW_ERROR:
                        break;
                }
            }
        };
        usbcard = new ReadCardApi(context, rcl);
        usbcard.setAnticollFlag(true);
        usbcard.startReadCard();
        usbcard.setH09CashBox(1);

    }

    @Override
    public void release() {
        super.release();
        hasOpenReadICCard.set(false);
        hasOpenScanQrCode.set(false);
        try {
            if (qrcodeSerialHelper != null) {
                qrcodeSerialHelper.close();
                qrcodeSerialHelper = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            qrcodeSerialHelper = null;
        }
        closeSerialScanGun();
        try {
            if (usbcard != null) {
                usbcard.closeCard();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDeviceName() {
        return "华壹_H09";
    }

    @SuppressLint("PrivateApi")
    @Override
    public String getMachineNumber() {
//        //通过反射获取sn号
        String serial = "";
        try {
            Class c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = String.valueOf(get.invoke(c, "ro.serialno"));
            if (!serial.equals("") && !serial.equals("unknown")) {
                return serial;
            }
            //9.0及以上无法获取到sn，此方法为补充，能够获取到多数高版本手机 sn
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serial = Build.getSerial();
                if (!TextUtils.isEmpty(serial)) {
                    return serial;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Build.SERIAL;
    }

    private AtomicBoolean hasOpenReadICCard = new AtomicBoolean();

    @Override
    public void readICCard(OnReadICCardListener readCardListener) {
        registerICCardListener(readCardListener);
        if (hasOpenReadICCard.get()) {
            return;
        }
    }

    public byte[] readM1Card() {
        try {

            byte[] btPwd = new byte[6];
            btPwd[0] = (byte) 0xFF;
            btPwd[1] = (byte) 0xFF;
            btPwd[2] = (byte) 0xFF;
            btPwd[3] = (byte) 0xFF;
            btPwd[4] = (byte) 0xFF;
            btPwd[5] = (byte) 0xFF;

            byte secNo = 13;
            byte mode = 1;//1:A 0:B
            byte blockNo = 55;
            int st = usbcard.cardverifyPwd(btPwd, secNo, mode);
            if (st == 0) {
                //PutMessage(secNo + " 扇区校验密码成功.");
                byte[] data = new byte[16];
                st = usbcard.cardReadBlock(blockNo, data);
                if (st >= 0) {
                    return data;
                } else {
                    notifyOnReadCardError("readM1Card: null");
                    usbcard.setAnticollFlag(false);
                    return null;
                }
            } else {
                usbcard.setAnticollFlag(false);
                notifyOnReadCardError("readM1Card: null");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            notifyOnReadCardError(e.getMessage());
            hasOpenReadICCard.set(false);
        }

        return null;
    }


    @Override
    public boolean isSupportReadICCard() {
        return true;
    }

    private AtomicBoolean hasOpenScanQrCode = new AtomicBoolean();

    @Override
    public void scanQrCode(OnScanQRCodeListener onScanQRCodeListener) {
        registerScanQRCodeListener(onScanQRCodeListener);
        if (hasOpenScanQrCode.get()) {
            return;
        }
        String sport = "/dev/ttyHSL0";
        try {
            qrcodeSerialHelper = new SerialHelper(sport, 9600) {
                @Override
                protected void onDataReceived(ComBean comBean) {
                    try {
                        String dataStr = new String(comBean.bRec, StandardCharsets.UTF_8);
                        LogHelper.print("--DeviceManager--" + sport + "--端口接收原始数据: " + dataStr);
                        String qrcodeData = dataStr.replace("\r", "");
                        if (dataStr.length() >= 4) {
                            notifyOnScanQrCode(qrcodeData);
                        } else {
                            notifyOnScanQRCodeError("扫码数据错误");
                        }
                        LogHelper.print("--DeviceManager--" + sport + "--端口接收卡数据: " + qrcodeData);
                        //关闭端口，避免重复刷
//                        close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        notifyOnScanQRCodeError("读取卡数据失败");
                    }
                }
            };
            qrcodeSerialHelper.open();
            hasOpenScanQrCode.set(true);
            LogHelper.print("----DeviceManager--" + sport + "打开端口成功");
        } catch (Throwable e) {
            e.printStackTrace();
            LogHelper.print("----DeviceManager--" + sport + "打开端口失败");
            notifyOnScanQRCodeError("端口" + sport + "打开失败");
            hasOpenScanQrCode.set(false);
        }
    }

    @Override
    public boolean isSupportScanQrCode() {
        return true;
    }

    @Override
    public boolean isSupportReadWeight() {
        return false;
    }

    @Override
    public boolean isSupportPrint() {
        return false;
    }

    @Override
    public boolean isSupportMoneyBox() {
        return false;
    }

    @Override
    public boolean rebootDevice() {
        (new CustomAPI(context)).reboot("reboot");
        return true;
    }

    @Override
    public boolean shutDownDevice() {
        (new CustomAPI(context)).shutDown(false,"shutdown",true);
        return true;
    }

    @Override
    public boolean showOrHideSysStatusBar(boolean showOrHide) {
        if (showOrHide) {
            (new CustomAPI(context)).switchStatusBar(1);
        } else {
            (new CustomAPI(context)).switchStatusBar(0);
        }

        return true;
    }

    @Override
    public boolean showOrHideSysNavBar(boolean showOrHide) {

        if (showOrHide) {
            (new CustomAPI(context)).switchNavBar(1);
        } else {
            (new CustomAPI(context)).switchNavBar(0);
        }
        return true;
    }

    @Override
    public boolean isSupportDualCamera() {
        return false;
    }

    @Override
    public boolean isSupportMobileSignal() {
        return true;
    }

    @Override
    public int getConsumeLayRes() {
        return 1;
    }

    @Override
    public int getCameraDisplayOrientation() {
        return 0;
    }


    @Override
    public int getIRCameraDisplayOrientation() {
        return 180;
    }

    @Override
    public boolean isSupportUSBDevice() {
        return true;
    }

    @Override
    public void initUsbDevices(HashMap<String, UsbDevice> usbDeviceMap) {
        for (Map.Entry<String, UsbDevice> usbDevice : usbDeviceMap.entrySet()) {
            UsbDevice usbDeviceValue = usbDevice.getValue();
            attachUsbDevice(usbDeviceValue);
        }
    }

    @Override
    public void attachUsbDevice(UsbDevice usbDevice) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                try {
                    if (keyBoardScanGunDeviceInfo.getVendorId() == usbDevice.getVendorId() && keyBoardScanGunDeviceInfo.getProductId() == usbDevice.getProductId()) {
                        //扫码枪-键盘
                        keyBoardScanGunDeviceInfo.setUsbDevice(usbDevice);
                        notifyAttachDevice(keyBoardScanGunDeviceInfo);
                        AppToast.toastMsg("扫码枪连接成功");
                    } else if (serialScanGunDeviceInfo.getVendorId() == usbDevice.getVendorId() && serialScanGunDeviceInfo.getProductId() == usbDevice.getProductId()) {
                        //扫码枪-串口
                        serialScanGunDeviceInfo.setUsbDevice(usbDevice);
                        notifyAttachDevice(serialScanGunDeviceInfo);
                        AppToast.toastMsg("扫码枪连接成功");
                        openSerialScanGun();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void detachUsbDevice(UsbDevice usbDevice) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                try {
                    if (keyBoardScanGunDeviceInfo.getVendorId() == usbDevice.getVendorId() && keyBoardScanGunDeviceInfo.getProductId() == usbDevice.getProductId()) {
                        //扫码枪-键盘
                        keyBoardScanGunDeviceInfo.setUsbDevice(null);
                        notifyDetachDevice(keyBoardScanGunDeviceInfo);
                        AppToast.toastMsg("扫码枪已断开");
                    } else if (serialScanGunDeviceInfo.getVendorId() == usbDevice.getVendorId() && serialScanGunDeviceInfo.getProductId() == usbDevice.getProductId()) {
                        //扫码枪-串口
                        serialScanGunDeviceInfo.setUsbDevice(null);
                        notifyDetachDevice(serialScanGunDeviceInfo);
                        AppToast.toastMsg("扫码枪已断开");
                        closeSerialScanGun();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 打开串口模式的扫码枪
     */
    private void openSerialScanGun(){
        String sport = "/dev/ttyACM0";
        try {
            scanGunSerialHelper = new SerialHelper(sport, 9600) {
                @Override
                protected void onDataReceived(ComBean comBean) {
                    try {
                        String dataStr = new String(comBean.bRec, StandardCharsets.UTF_8);
                        LogHelper.print("--DeviceManager--" + sport + "--端口接收原始数据: " + dataStr);
                        String qrcodeData = dataStr.replace("\r", "");
                        if (dataStr.length() >= 4) {
                            notifyOnScanQrCode(qrcodeData);
                        } else {
                            notifyOnScanQRCodeError("扫码数据错误");
                        }
                        LogHelper.print("--DeviceManager--" + sport + "--端口接收卡数据: " + qrcodeData);
                        //关闭端口，避免重复刷
//                        close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        notifyOnScanQRCodeError("读取卡数据失败");
                    }
                }
            };
            scanGunSerialHelper.open();
            LogHelper.print("----DeviceManager--" + sport + "打开端口成功");
        } catch (Throwable e) {
            e.printStackTrace();
            LogHelper.print("----DeviceManager--" + sport + "打开端口失败");
            notifyOnScanQRCodeError("端口" + sport + "打开失败");
        }
    }

    /**
     * 关闭串口模式的扫码枪
     */
    private void closeSerialScanGun(){
        try {
            if (scanGunSerialHelper != null) {
                scanGunSerialHelper.close();
                scanGunSerialHelper = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            scanGunSerialHelper = null;
        }
    }

    @Override
    public List<DeviceHardwareInfo> getUSBDeviceHardwareInfoList() {
        return mUSBHardwareInfoList;
    }

    @Override
    public boolean isCanDispatchKeyEvent() {
        UsbDevice usbDevice = keyBoardScanGunDeviceInfo.getUsbDevice();
        if (usbDevice != null) {
            LogHelper.print("--dispatchKeyEvent--isCanDispatchKeyEvent true");
            return true;
        }
        LogHelper.print("--dispatchKeyEvent--isCanDispatchKeyEvent false");
        return false;
    }

    @Override
    public boolean isFinishDispatchKeyEvent() {
        return scanKeyManager.isFinishOnceScan();
    }

    @Override
    public void dispatchKeyEvent(KeyEvent event) {
        if (scanKeyManager != null) {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                scanKeyManager.setFinishOnceScan(true);
            } else {
                scanKeyManager.setFinishOnceScan(false);
            }
            scanKeyManager.analysisKeyEvent(event);
        }
    }

    @Override
    public void onScanValue(String value) {
        LogHelper.print("---DeviceManager-onScanValue value: " + value);
        notifyOnScanQrCode(value);
    }

    @Override
    public int get50cmDetectFaceMinThreshold() {
        return 220;
    }

    @Override
    public int get80cmDetectFaceMinThreshold() {
        return 300;
    }

    @Override
    public int get100cmDetectFaceMinThreshold() {
        return 340;
    }

    @Override
    public void silenceInstallApk(String apkPath) {
        (new CustomAPI(context)).silentInstall(apkPath,true);
    }
}
