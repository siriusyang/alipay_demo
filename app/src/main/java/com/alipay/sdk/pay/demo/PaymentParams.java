package com.alipay.sdk.pay.demo;

/**
 * Created by bao.yang on 6/29/2018.
 */

public class PaymentParams {
    public Params payment_params;

    static class Params {
        public String subject;
        public String out_trade_no;
        public String total_amount;
        public String product_code;
        public String seller_id;
    }
}
