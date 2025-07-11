package com.stkj.cashier.machine.bind;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stkj.cashier.R;
import com.stkj.cashier.base.callback.OnConsumerConfirmListener;
import com.stkj.cashier.base.device.DeviceManager;
import com.stkj.cashier.base.ui.adapter.FoodListShowAdapter;
import com.stkj.cashier.base.ui.widget.FacePassCameraLayout;
import com.stkj.cashier.base.utils.EventBusUtils;
import com.stkj.cashier.consumer.ConsumerManager;
import com.stkj.cashier.consumer.callback.ConsumerController;
import com.stkj.cashier.consumer.callback.ConsumerListener;
import com.stkj.cashier.consumer.callback.OnInputNumberListener;
import com.stkj.cashier.consumer.ui.weight.SimpleInputNumber;
import com.stkj.cashier.home.callback.OnGetStoreInfoListener;
import com.stkj.cashier.home.model.StoreInfo;
import com.stkj.cashier.home.ui.activity.MainActivity;
import com.stkj.cashier.home.ui.widget.BindingHomeTitleLayout;
import com.stkj.cashier.home.ui.widget.HomeTitleLayout;
import com.stkj.cashier.pay.callback.OnConsumerModeListener;
import com.stkj.cashier.pay.data.PayConstants;
import com.stkj.cashier.pay.helper.ConsumerModeHelper;
import com.stkj.cashier.pay.model.ChangeConsumerModeEvent;
import com.stkj.cashier.pay.model.RefreshConsumerGoodsModeEvent;
import com.stkj.cashier.pay.ui.fragment.AddGoodsFragment;
import com.stkj.cashier.pay.ui.fragment.AmountConsumerFragment;
import com.stkj.cashier.pay.ui.fragment.BasePayHelperFragment;
import com.stkj.cashier.pay.ui.fragment.GoodsConsumerFragment;
import com.stkj.cashier.pay.ui.fragment.NumberConsumerFragment;
import com.stkj.cashier.pay.ui.fragment.TakeMealConsumerFragment;
import com.stkj.cashier.pay.ui.fragment.WeightConsumerFragment;
import com.stkj.cashier.setting.helper.StoreInfoHelper;
import com.stkj.cashier.setting.model.FacePassPeopleInfo;
import com.stkj.common.core.AppManager;
import com.stkj.common.ui.fragment.BaseRecyclerFragment;
import com.stkj.common.ui.widget.common.CircleProgressBar;
import com.stkj.common.ui.widget.shapelayout.ShapeFrameLayout;
import com.stkj.common.ui.widget.shapelayout.ShapeTextView;
import com.stkj.common.utils.FragmentUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 绑盘机页面——首页
 */
public class TabBindHomeFragment extends BasePayHelperFragment implements OnConsumerModeListener , ConsumerController {

    public final static String TAG = "TabBindHomeFragment";
    //    private LinearLayout llOrderList;
//    private RecyclerView rvGoodsList;
//    private LinearLayout llFastPayPresentation;
//    private TextView tvGoodsCount;
//    private TextView tvGoodsPrice;
//    private ShapeFrameLayout sflOrderList;
//    private CommonRecyclerAdapter mOrderAdapter;
    private ShapeFrameLayout sflConsumerContent;
    private FacePassCameraLayout fpcFace;

    private ConsumerListener consumerListener;
    private OnConsumerConfirmListener facePassConfirmListener;
    private boolean isSetPayOrderInfo;
    private LinearLayout llFaceConfirm;
    private ShapeTextView stvFaceLeftBt;
    private ShapeTextView stvFaceRightBt;
    private ShapeTextView stvPayPrice;
    private ShapeTextView stv_pay_price_balance;
    private LinearLayout llTakeMealWay;
    private ShapeTextView stvTakeMealByCode;
    private ShapeTextView stvTakeMealByPhone;
    private SimpleInputNumber sinNumber;
    private ShapeFrameLayout sflInputNumber;
    private CircleProgressBar pbConsumer;
    private ShapeTextView stvCancelPay;
    private Context context;
    private int currentConsumerMode;
    private boolean isConsumerAuthTips;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_bind;
    }

    @Override
    protected void onFragmentResume(boolean isFirstOnResume) {
        EventBusUtils.registerEventBus(this);
        ConsumerModeHelper consumerModeHelper = new ConsumerModeHelper(AppManager.INSTANCE.getMainActivity());
        currentConsumerMode = consumerModeHelper.getCurrentConsumerMode();
        findViews();

    }

    private void findViews() {
        if (DeviceManager.INSTANCE.getDeviceInterface().getConsumeLayRes() != 2) {



            stvCancelPay = (ShapeTextView) findViewById(R.id.stv_cancel_pay);
            pbConsumer = (CircleProgressBar) findViewById(R.id.pb_consumer);
            sflInputNumber = (ShapeFrameLayout) findViewById(R.id.sfl_input_number);
            sinNumber = (SimpleInputNumber) findViewById(R.id.sin_number);
            stvPayPrice = (ShapeTextView) findViewById(R.id.stv_pay_price);
            stv_pay_price_balance = (ShapeTextView) findViewById(R.id.stv_pay_price_balance);
            sflConsumerContent = (ShapeFrameLayout) findViewById(R.id.sfl_consumer_content);
            fpcFace = (FacePassCameraLayout) findViewById(R.id.fpc_face);
            llFaceConfirm = (LinearLayout) findViewById(R.id.ll_face_confirm);
            stvFaceLeftBt = (ShapeTextView) findViewById(R.id.stv_face_left_bt);
            stvFaceRightBt = (ShapeTextView) findViewById(R.id.stv_face_right_bt);
            if (currentConsumerMode == PayConstants.CONSUMER_GOODS_MODE){
                sflConsumerContent.setBackground(getResources().getDrawable(R.mipmap.goods_face_bg));
                stvPayPrice.setSolidColor(Color.parseColor("#00000000"));
                //sflConsumerContent.setRadius(getResources().getDimensionPixelSize(com.stkj.common.R.dimen.dp_5));
            }else {
                sflConsumerContent.setBackground(null);
                stvPayPrice.setSolidColor(Color.parseColor("#e9f3ff"));
                //sflConsumerContent.setRadius(getResources().getDimensionPixelSize(com.stkj.common.R.dimen.dp_0));
            }
        }

        if (consumerListener != null) {
            Log.d(TAG, "limefindViews: " + 187);
            consumerListener.onCreateFacePreviewView(fpcFace.getFacePreviewFace(), fpcFace.getIrPreviewFace());
        }

//        sflOrderList = (ShapeFrameLayout) findViewById(R.id.sfl_order_list);
//        rvGoodsList = (RecyclerView) findViewById(R.id.rv_goods_list);
//        llFastPayPresentation = (LinearLayout) findViewById(R.id.ll_fast_pay_presentation);
//        tvGoodsCount = (TextView) findViewById(R.id.tv_goods_count);
//        tvGoodsPrice = (TextView) findViewById(R.id.tv_goods_price);
//        mOrderAdapter = new CommonRecyclerAdapter(false);
//        rvGoodsList.setAdapter(mOrderAdapter);
        llTakeMealWay = (LinearLayout) findViewById(R.id.ll_take_meal_way);
        stvTakeMealByCode = (ShapeTextView) findViewById(R.id.stv_take_meal_by_code);
        stvTakeMealByCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facePassConfirmListener != null) {
                    facePassConfirmListener.onShowSimpleInputNumber(true);
                }
                sflInputNumber.setVisibility(View.VISIBLE);
                sinNumber.setInputNumberCount(4);
            }
        });
        stvTakeMealByPhone = (ShapeTextView) findViewById(R.id.stv_take_meal_by_phone);
        stvTakeMealByPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facePassConfirmListener != null) {
                    facePassConfirmListener.onShowSimpleInputNumber(true);
                }
                sflInputNumber.setVisibility(View.VISIBLE);
                sinNumber.setInputNumberCount(11);
            }
        });
        sinNumber.setInputNumberListener(new OnInputNumberListener() {
            @Override
            public void onConfirmNumber(String number) {
                sflInputNumber.setVisibility(View.GONE);
                if (facePassConfirmListener != null) {
                    int inputNumberCount = sinNumber.getInputNumberCount();
                    if (inputNumberCount == 4) {
                        facePassConfirmListener.onConfirmTakeMealCode(number);
                    } else if (inputNumberCount == 11) {
                        facePassConfirmListener.onConfirmPhone(number);
                    }
                }
            }

            @Override
            public void onClickBack() {
                if (facePassConfirmListener != null) {
                    facePassConfirmListener.onShowSimpleInputNumber(false);
                }
                sflInputNumber.setVisibility(View.GONE);
            }

            @Override
            public void onConfirmError(boolean hasInputNumber) {
                int inputNumberCount = sinNumber.getInputNumberCount();
                if (inputNumberCount == 4) {
                    //speakTTSVoice(hasInputNumber ? "请输入完整的取餐码" : "请输入取餐码");
                } else if (inputNumberCount == 11) {
                    //speakTTSVoice(hasInputNumber ? "请输入完整的手机号" : "请输入手机号");
                }
            }
        });

        ConsumerManager.INSTANCE.setConsumerTips("欢迎光临!");
    }

    /**
     * 切换餐厅模式
     */
    private void changeConsumerMode(int mode) {
        if (mode == PayConstants.CONSUMER_AMOUNT_MODE) {
            //默认金额模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new AmountConsumerFragment(), R.id.fl_pay_second_content);
        } else if (mode == PayConstants.CONSUMER_NUMBER_MODE) {
            //按次模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new NumberConsumerFragment(), R.id.fl_pay_second_content);
        } else if (mode == PayConstants.CONSUMER_TAKE_MODE) {
            //取餐模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new TakeMealConsumerFragment(), R.id.fl_pay_second_content);
        } else if (mode == PayConstants.CONSUMER_SEND_MODE) {
            //送餐模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new TakeMealConsumerFragment(), R.id.fl_pay_second_content);
        } else if (mode == PayConstants.CONSUMER_WEIGHT_MODE) {
            //称重模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new WeightConsumerFragment(), R.id.fl_pay_second_content);
        } else if (mode == PayConstants.CONSUMER_GOODS_MODE) {
            //商品模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new GoodsConsumerFragment(), R.id.fl_pay_second_content);
        }else {
            //默认金额模式
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new AmountConsumerFragment(), R.id.fl_pay_second_content);
        }

        EventBus.getDefault().post(new ChangeConsumerModeEvent(mode));

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshConsumerGoodsModeEvent(RefreshConsumerGoodsModeEvent eventBus) {
        if (eventBus.getPageMode() == 1){
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new AddGoodsFragment(), R.id.fl_pay_second_content);
        } else {
            FragmentUtils.safeReplaceFragment(getChildFragmentManager(), new GoodsConsumerFragment(), R.id.fl_pay_second_content);
        }
    }


    @Override
    public void onChangeConsumerMode(int consumerMode, int lastConsumerMode) {
        changeConsumerMode(consumerMode);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBusUtils.unRegisterEventBus(this);
    }

    @Override
    public void setFacePreview(boolean preview) {
        if (fpcFace != null) {
            fpcFace.setPreviewFace(preview);
        }
    }

    @Override
    public void setConsumerAuthTips(String tips) {
        if (fpcFace != null) {
            isConsumerAuthTips = true;
            fpcFace.setFaceCameraTips(tips);
        }
    }

    @Override
    public void setConsumerTips(String tips) {
        setConsumerTips(tips, 0);
    }

    @Override
    public void setConsumerTips(String tips, int consumerPro) {
        if (fpcFace != null) {
            isConsumerAuthTips = false;
            fpcFace.setFaceCameraTips(tips);
            if (consumerPro > 0) {
                pbConsumer.setVisibility(View.VISIBLE);
                pbConsumer.setProgress(consumerPro);
            } else {
                pbConsumer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean isConsumerAuthTips() {
        return isConsumerAuthTips;
    }

    @Override
    public void setConsumerConfirmFaceInfo(FacePassPeopleInfo facePassPeopleInfo, boolean needConfirm, int consumerType) {
        stvCancelPay.setVisibility(View.GONE);
        pbConsumer.setVisibility(View.GONE);
        llTakeMealWay.setVisibility(View.GONE);
        fpcFace.setFaceImage(facePassPeopleInfo.getImgData());
        if (needConfirm) {
            fpcFace.setFaceCameraTips("识别成功,请确认?");
            llFaceConfirm.setVisibility(View.VISIBLE);
            stvFaceLeftBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        if (consumerType == PayConstants.PAY_TYPE_IC_CARD) {
                            facePassConfirmListener.onConfirmCardNumber(facePassPeopleInfo.getCard_Number());
                        } else {
                            facePassConfirmListener.onConfirmFacePass(facePassPeopleInfo);
                        }
                    }
                    llFaceConfirm.setVisibility(View.GONE);
                }
            });
            stvFaceRightBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        if (consumerType == PayConstants.PAY_TYPE_IC_CARD) {
                            facePassConfirmListener.onCancelCardNumber(facePassPeopleInfo.getCard_Number());
                        } else {
                            facePassConfirmListener.onCancelFacePass(facePassPeopleInfo);
                        }
                    }
                }
            });
        } else {
            if (facePassConfirmListener != null) {
                if (consumerType == PayConstants.PAY_TYPE_IC_CARD) {
                    facePassConfirmListener.onConfirmCardNumber(facePassPeopleInfo.getCard_Number());
                } else {
                    facePassConfirmListener.onConfirmFacePass(facePassPeopleInfo);
                }
            }
            llFaceConfirm.setVisibility(View.GONE);
        }
    }

    @Override
    public void setConsumerConfirmCardInfo(String cardNumber, boolean needConfirm) {
        stvCancelPay.setVisibility(View.GONE);
        pbConsumer.setVisibility(View.GONE);
        llTakeMealWay.setVisibility(View.GONE);
        fpcFace.setFaceImage("");
        if (needConfirm) {
            fpcFace.setFaceCameraTips("读卡成功,请确认?");
            llFaceConfirm.setVisibility(View.VISIBLE);
            stvFaceLeftBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        facePassConfirmListener.onConfirmCardNumber(cardNumber);
                    }
                    llFaceConfirm.setVisibility(View.GONE);
                }
            });
            stvFaceRightBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        facePassConfirmListener.onCancelCardNumber(cardNumber);
                    }
                }
            });
        } else {
            if (facePassConfirmListener != null) {
                facePassConfirmListener.onConfirmCardNumber(cardNumber);
            }
        }
    }

    @Override
    public void setConsumerConfirmScanInfo(String scanData, boolean needConfirm) {
        stvCancelPay.setVisibility(View.GONE);
        pbConsumer.setVisibility(View.GONE);
        llTakeMealWay.setVisibility(View.GONE);
        fpcFace.setFaceImage("");
        if (needConfirm) {
            fpcFace.setFaceCameraTips("扫码成功,请确认?");
            llFaceConfirm.setVisibility(View.VISIBLE);
            stvFaceLeftBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        facePassConfirmListener.onConfirmScanData(scanData);
                    }
                    llFaceConfirm.setVisibility(View.GONE);
                }
            });
            stvFaceRightBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        facePassConfirmListener.onCancelScanData(scanData);
                    }
                }
            });
        } else {
            if (facePassConfirmListener != null) {
                facePassConfirmListener.onConfirmScanData(scanData);
            }
        }
    }

    @Override
    public void setConsumerTakeMealWay() {
        pbConsumer.setVisibility(View.GONE);
        llTakeMealWay.setVisibility(View.VISIBLE);
    }

    @Override
    public void resetFaceConsumerLayout() {
        if (fpcFace != null) {
            stvCancelPay.setVisibility(View.GONE);
            pbConsumer.setVisibility(View.GONE);
            sflInputNumber.setVisibility(View.GONE);
            llTakeMealWay.setVisibility(View.GONE);
            llFaceConfirm.setVisibility(View.GONE);
            stvPayPrice.setText("¥ 0.00");
            stvPayPrice.setVisibility(View.GONE);
        }
    }

    @Override
    public void setNormalConsumeStatus() {
        stvCancelPay.setVisibility(View.GONE);
        pbConsumer.setVisibility(View.GONE);
        sflInputNumber.setVisibility(View.GONE);
        llTakeMealWay.setVisibility(View.GONE);
//        sflOrderList.setVisibility(View.GONE);
    }

    @Override
    public void setPayConsumeStatus() {
        stvCancelPay.setVisibility(View.GONE);
        pbConsumer.setVisibility(View.GONE);
        sflInputNumber.setVisibility(View.GONE);
        llTakeMealWay.setVisibility(View.GONE);
//        sflOrderList.setVisibility(isSetPayOrderInfo ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPayPrice(String payPrice, boolean showCancelPay) {
        setCanCancelPay(showCancelPay);
        pbConsumer.setVisibility(View.GONE);
        stvPayPrice.setVisibility(View.VISIBLE);
        stvPayPrice.setText("¥ " + payPrice);
    }

    @Override
    public void setCanCancelPay(boolean showCancelPay) {
        if (showCancelPay) {
            stvCancelPay.setVisibility(View.VISIBLE);
            stvCancelPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (facePassConfirmListener != null) {
                        facePassConfirmListener.onConsumerCancelPay();
                    }
                }
            });
        } else {
            stvCancelPay.setVisibility(View.GONE);
        }
    }
}