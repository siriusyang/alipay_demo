package com.alipay.sdk.pay.demo;

import java.io.IOException;
import java.util.Map;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.pay.demo.util.OkHttpUtil;
import com.alipay.sdk.pay.demo.util.OrderInfoUtil2_0;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 重要说明:
 * <p>
 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
 */
public class PayDemoActivity extends FragmentActivity {
    TextInputEditText url;
    TextInputEditText subject;
    TextInputEditText out_trade_no;
    TextInputEditText product_code;
    TextInputEditText seller_id;
    TextInputEditText total_amount;
    private TextView payLog;
    /**
     * 支付宝支付业务：入参app_id
     */
    public static final String APPID = "";

    /**
     * 支付宝账户登录授权业务：入参pid值
     */
    public static final String PID = "";
    /**
     * 支付宝账户登录授权业务：入参target_id值
     */
    public static final String TARGET_ID = "";

    /** 商户私钥，pkcs8格式 */
    /** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
    /** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
    /** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
    /** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
    /**
     * 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1
     */
    public static final String RSA2_PRIVATE = "";
    public static final String RSA_PRIVATE = "";

    private static final int SDK_PAY_FLAG = 1;
    private static final int ORDER_INFO = 3;
    private static final int SDK_AUTH_FLAG = 2;
    private String orderInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_external);
        url = findViewById(R.id.url);
        subject = findViewById(R.id.subject);
        out_trade_no = findViewById(R.id.out_trade_no);
        product_code = findViewById(R.id.product_code);
        seller_id = findViewById(R.id.seller_id);
        total_amount = findViewById(R.id.total_amount);
        payLog = findViewById(R.id.payLog);
        setData();
    }

    private void setData() {
        long time = System.currentTimeMillis();
        subject.setText("subject" + time);
        out_trade_no.setText("out_trade_no" + time);
        product_code.setText("product_code" + time);
        seller_id.setText("" + time);
    }

    /**
     * 支付宝支付业务
     *
     * @param v
     */
    public void payV2(View v) {
        setData();
        payLog.setText("");
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String getOrderInfoUrl = "http://e103d809.ngrok.io/payment/v1/payment";
                    String url2 = url.getText().toString();
                    if (url2 != null & url2.length() > 0 && url2.contains("http")) {
                        getOrderInfoUrl = url2;
                    } else if (url2 != null & url2.length() > 0) {
                        getOrderInfoUrl = "http://" + url2 + ".ngrok.io/payment/v1/payment";
                    }

                    PaymentParams paymentParams = new PaymentParams();
                    PaymentParams.Params params = new PaymentParams.Params();
                    params.out_trade_no = out_trade_no.getText().toString();
                    params.product_code = product_code.getText().toString();
                    params.seller_id = seller_id.getText().toString();
                    params.subject = subject.getText().toString();
                    params.total_amount = total_amount.getText().toString();
                    paymentParams.payment_params = params;
                    orderInfo = OkHttpUtil.getSingeInfo(getOrderInfoUrl, paymentParams);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("orderInfo", orderInfo);

                PayTask alipay = new PayTask(PayDemoActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.e("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
                Message msg2 = new Message();
                msg2.what = ORDER_INFO;
                msg2.obj = orderInfo;
                mHandler.sendMessage(msg2);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ORDER_INFO: {
                    payLog.setText(msg.obj.toString());
                    break;
                }
                case SDK_PAY_FLAG: {

                    @SuppressWarnings("unchecked")
                    Map<String, String> resultMap = (Map<String, String>) msg.obj;
//                    payLog.setText(resultMap.toString());
                    PayResult payResult = new PayResult(resultMap);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(PayDemoActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(PayDemoActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        Toast.makeText(PayDemoActivity.this,
                                "授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // 其他状态值则为授权失败
                        Toast.makeText(PayDemoActivity.this,
                                "授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show();

                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

}
