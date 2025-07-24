package com.stkj.platebind.pay.goods.callback;


import com.stkj.platebind.pay.goods.model.GoodsCate;

import java.util.List;

public interface OnGetGoodsCateListListener {
    void onGetCateListSuccess(int goodsType, List<GoodsCate> goodsCateList);

    default void onGetCateListError(int goodsType, String msg) {

    }
}
