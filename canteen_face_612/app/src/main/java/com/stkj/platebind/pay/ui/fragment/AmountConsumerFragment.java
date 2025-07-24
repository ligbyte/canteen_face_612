package com.stkj.platebind.pay.ui.fragment;

import android.view.View;

import androidx.annotation.Nullable;

import com.stkj.platebind.MainApplication;
import com.stkj.platebind.R;
import com.stkj.platebind.base.callback.OnConsumerConfirmListener;
import com.stkj.platebind.base.utils.CommonDialogUtils;
import com.stkj.platebind.base.utils.EventBusUtils;
import com.stkj.platebind.base.utils.PriceUtils;
import com.stkj.platebind.consumer.ConsumerManager;
import com.stkj.platebind.pay.callback.OnCalculateListener;
import com.stkj.platebind.pay.callback.OnPayListener;
import com.stkj.platebind.pay.data.PayConstants;
import com.stkj.platebind.pay.helper.ConsumerModeHelper;
import com.stkj.platebind.pay.model.FacePassRetryEvent;
import com.stkj.platebind.pay.model.ModifyBalanceResult;
import com.stkj.platebind.pay.model.RefreshConsumerAmountModeEvent;
import com.stkj.platebind.pay.ui.weight.SimpleCalculator;
import com.stkj.platebind.setting.model.FacePassPeopleInfo;
import com.stkj.cbgfacepass.CBGFacePassHandlerHelper;
import com.stkj.common.core.AppManager;
import com.stkj.common.utils.FragmentUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

/**
 * 金额模式
 */
public class AmountConsumerFragment extends BasePayHelperFragment implements OnPayListener, OnConsumerConfirmListener {

    private SimpleCalculator scCalc;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_amount_consumer;
    }

    @Override
    protected void initViews(View rootView) {
        EventBusUtils.registerEventBus(this);
        scCalc = (SimpleCalculator) findViewById(R.id.sc_calc);
        scCalc.setOnCalculateListener(new OnCalculateListener() {
            @Override
            public void onConfirmMoney(String payMoney) {
                goToAmountPay();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onClickDisableConfirm() {
                stopAmountPay();
            }
        });
    }

    @Override
    public void onCancelCardNumber(String cardNumber) {
        super.onCancelCardNumber(cardNumber);
        scCalc.setCalcEnable(true);
        scCalc.setConfirmTxt("结算");
    }

    @Override
    public void onCancelFacePass(FacePassPeopleInfo passPeopleInfo) {
        super.onCancelFacePass(passPeopleInfo);
        scCalc.setCalcEnable(true);
        scCalc.setConfirmTxt("结算");
    }

    /**
     * 获取最终结算价格
     */
    private String getAmountRealPayMoney() {
        String currentInputText = scCalc.getCurrentInputText();
        return PriceUtils.formatPrice(currentInputText);
    }

    /**
     * 金额结算
     */
    private void goToAmountPay() {
        CBGFacePassHandlerHelper.imageCache = null;
        ConsumerManager.INSTANCE.resetFaceConsumerLayout();
        String realPayMoney = getAmountRealPayMoney();
        int goToPay = goToPay(realPayMoney);
        if (goToPay == NORMAL_TO_PAY) {
            scCalc.setCalcEnable(false);
            scCalc.setConfirmTxt("取消结算");

        } else {
            CommonDialogUtils.showTipsDialog(mActivity, getGoToPayStatus(goToPay));
        }
    }

    /**
     * 取消金额结算
     */
    private void stopAmountPay() {

        MainApplication.isNeedCache = false;
        speakTTSVoice("取消结算");
        stopToPay();
        scCalc.setCalcEnable(true);
        scCalc.setConfirmTxt("结算");
    }

    @Override
    protected void onFragmentResume(boolean isFirstOnResume) {
        if (isFirstOnResume) {
            EventBusUtils.registerEventBus(this);
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new ConsumerRecordListFragment(), R.id.fl_consumer_list_content);
        }
    }

    @Override
    public void onPayError(String responseCode, Map<String, String> payRequest, @Nullable ModifyBalanceResult modifyBalanceResult, String msg) {
        super.onPayError(responseCode, payRequest, modifyBalanceResult, msg);
        scCalc.setCalcEnable(true);
        scCalc.setConfirmTxt("结算");
    }

    @Override
    public void onPaySuccess(Map<String, String> payRequest, ModifyBalanceResult modifyBalanceResult) {
        super.onPaySuccess(payRequest, modifyBalanceResult);
        scCalc.setCalcEnable(true);
        scCalc.setConfirmTxt("结算");
        scCalc.clearCalcData();
    }

    @Override
    protected void onPayCancel(int payType) {
        stopAmountPay();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshConsumerAmountModeEvent(RefreshConsumerAmountModeEvent eventBus) {
        stopAmountPay();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleFaceRetry(FacePassRetryEvent eventBus) {
        ConsumerModeHelper consumerModeHelper = new ConsumerModeHelper(AppManager.INSTANCE.getMainActivity());
        int currentConsumerMode = consumerModeHelper.getCurrentConsumerMode();
        if (currentConsumerMode == PayConstants.CONSUMER_AMOUNT_MODE) {
            goToAmountPay();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        stopToPay();
        EventBusUtils.unRegisterEventBus(this);
    }
}
