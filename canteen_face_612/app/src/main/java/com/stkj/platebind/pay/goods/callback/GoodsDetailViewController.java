package com.stkj.platebind.pay.goods.callback;


import com.stkj.platebind.pay.goods.model.GoodsEditBaseInfo;
import com.stkj.platebind.pay.goods.model.GoodsQrCodeDetail;

public interface GoodsDetailViewController {

    GoodsEditBaseInfo getGoodsDetailEditInfo();

    void setGoodsDetailEditMode();

    void setGoodsQrCodeInfo(GoodsQrCodeDetail goodsQrCodeInfo);

}
