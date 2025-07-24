package com.stkj.cashier.pay.model;

public class LoadingDialogEvent {


    private String msg;
    private String tag;

    public LoadingDialogEvent(String msg, String tag) {
        this.msg = msg;
        this.tag = tag;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
