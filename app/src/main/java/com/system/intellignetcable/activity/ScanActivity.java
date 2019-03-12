package com.system.intellignetcable.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.system.intellignetcable.R;
import com.system.intellignetcable.adapter.ScanAdapter;
import com.system.intellignetcable.bean.SignageManagementBean;
import com.system.intellignetcable.util.ParamUtil;
import com.system.intellignetcable.util.SharedPreferencesUtil;
import com.system.intellignetcable.util.UrlUtils;
import com.system.intellignetcable.util.Util;
import com.uhf.scanlable.UHfData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by adu on 2018/11/30.
 */

public class ScanActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    @BindView(R.id.back_iv)
    ImageView backIv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.scan_lv)
    ListView scanLv;
    private static String[] CARDS = {"ZFY000001", "ZFY000002", "ZFY000003",
            "ZFY000004", "ZFY000005", "ZFY000006",
    };
    private ScanAdapter scanAdapter;
    private static final int MESSAGE_SUCCESS = 0;
    private static final int MESSAGE_FAIL = 1;
    private static final String devport = "/dev/ttyMT1";
    private List<UHfData.InventoryTagMap> data;
    private static final int selectedEd = 1; //盘存模式，0—单标签，1—多标签，2—自动
    private static final int tidflag = 0;//是否盘存TID，0—否，1—是，值为1时盘存到的为标签的TID


    private boolean isCanceled = true;
    private Timer timer;
    private static boolean Scanflag = false;


    //获得数据
    private static final int MSG_UPDATE_LISTVIEW = 0;
    //扫描间隔
    private static final int SCAN_INTERVAL = 5;

    private boolean isConnected = true;

    private boolean isEdit;
    private int mWorkId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_scan);
        ButterKnife.bind(this);
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        mWorkId = getIntent().getIntExtra("workId", -1);
        initData();
        Util.initSoundPool(this);
    }

    private void initData() {
        new UHfData(this);
        titleTv.setText(R.string.scan);
        data = new ArrayList<>();
        scanAdapter = new ScanAdapter(data);
        scanAdapter.setOnBtnClickListener(new ScanAdapter.onBtnClickListener() {
            @Override
            public void onClick(String epcId) {
                if (isConnected) {
                    showProgressLoading();
                    signageManagementDetail(epcId);
                } else {
                    Toast.makeText(ScanActivity.this, "不是串口设备，无法亮灯",
                            Toast.LENGTH_SHORT).show();
                }


//                lighting(epcId);
            }
        });
        scanLv.setAdapter(scanAdapter);
        scanLv.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UHfData.lsTagList.clear();
        UHfData.dtIndexMap.clear();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            public void run() {
                try {
                    int result = UHfData.UHfGetData.OpenUHf(devport, 57600);
                    if (result == 0) {
                        mConnectHandler.sendEmptyMessage(MESSAGE_SUCCESS);
                        Thread.sleep(1000);
                        int carrierTime = Util.getSavedCarrier(ScanActivity.this);
                        String errorCode = UHfData.UHfGetData.setCarrierTimeOperation(carrierTime);
                        Log.i(ScanActivity.class.getSimpleName(), "setCarrierTimeOperation = " + errorCode);
                    } else {
                        mConnectHandler.sendEmptyMessage(MESSAGE_FAIL);
                    }
                } catch (Exception e) {
                    mConnectHandler.sendEmptyMessage(MESSAGE_FAIL);
                }
            }
        }).start();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelScan();
        unregisterReceiver(keyReceiver);
    }

    @OnClick(R.id.back_iv)
    public void onViewClicked() {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ScanActivity.this, SignageManagementActivity.class);
        if (data.size() > 0) {
            intent.putExtra("epcId", data.get(position).strEPC);
        }
        intent.putExtra("workId", mWorkId);
        intent.putExtra("isEdit", isEdit);
        startActivity(intent);
    }

    private ScanHandler mHandler = new ScanHandler(this);


    private static class ScanHandler extends Handler {
        private WeakReference<ScanActivity> mReference;
        private ScanActivity mActivity;

        ScanHandler(ScanActivity activity) {
            mReference = new WeakReference<>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.isCanceled)
                return;
            switch (msg.what) {
                case MSG_UPDATE_LISTVIEW:
                    mActivity.data = UHfData.lsTagList;
                    mActivity.scanAdapter.setData(mActivity.data);
                    if (UHfData.mIsNew) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (!Util.soundfinished)
                                    Util.play(1, 0);
                            }
                        }).start();
                        UHfData.mIsNew = false;
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private ConnectHandler mConnectHandler = new ConnectHandler(this);

    private static class ConnectHandler extends Handler {
        private WeakReference<ScanActivity> mReference;
        private ScanActivity mActivity;

        ConnectHandler(ScanActivity activity) {
            mReference = new WeakReference<>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SUCCESS) {
                Toast.makeText(mActivity.getApplicationContext(), "串口连接成功",
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == MESSAGE_FAIL) {
                Toast.makeText(mActivity.getApplicationContext(), "串口连接失败",
                        Toast.LENGTH_SHORT).show();
                // 显示假数据
                mActivity.showData();
                mActivity.isConnected = false;
            }
            super.handleMessage(msg);
        }
    }


    private void scan() {
        try {
            if (timer == null) {
                UHfData.lsTagList.clear();
                UHfData.dtIndexMap.clear();
                scanAdapter.clearData();
                mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
                mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                isCanceled = false;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (Scanflag)
                            return;
                        Scanflag = true;
                        UHfData.Inventory_6c(selectedEd, tidflag);
                        mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
                        mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                        Scanflag = false;
                    }
                }, 0, SCAN_INTERVAL);
                //TODO 正在扫描
//                scan.setText(R.string.stop);
            } else {
                isCanceled = true;
                timer.cancel();
                timer = null;
                //TODO 停止扫描
//                    scan.setText(R.string.scan);
            }
        } catch (Exception e) {
        }
    }


    private void cancelScan() {
        isCanceled = true;
        mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
        if (isConnected) {
            String closeRF = UHfData.UHfGetData.closeRF();
            Log.i("ScanActivity", "closeRF when back = " + closeRF);
            UHfData.lsTagList.clear();
            UHfData.dtIndexMap.clear();
        }
        scanAdapter.clearData();
        if (timer != null) {
            timer.cancel();
            timer = null;
//            scan.setText(R.string.scan);
        }
    }

    /**
     * 终端设备下边按钮点击事件
     */
    private long startTime = 0;
    private boolean KeyUpFlag = true;
    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            // H941
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (KeyUpFlag && keyDown && System.currentTimeMillis() - startTime > 500) {
                KeyUpFlag = false;
                startTime = System.currentTimeMillis();
                if (keyCode == KeyEvent.KEYCODE_F3) {
                    //TODO 扫描事件
                    scan();
                }
            } else if (keyDown) {
                startTime = System.currentTimeMillis();
            } else {
                KeyUpFlag = true;
            }

        }
    };

    private void lighting(final String etcId, final String pwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sendCmdA1 = UHfData.UHfGetData.sendCmdA1(etcId, pwd, 25);
                if (sendCmdA1) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplication(), "亮灯成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplication(), "亮灯失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
//        try {
//            int wordPtr = 32;
//            int len = 1;
//            String contentStr = "0800";
//            byte[] word = Tools.intToByte(wordPtr);
//            byte[] pwdBytes = Tools.HexString2Bytes("12345678");
//            int result = UHfData.UHfGetData.Write6c((byte) len, (byte) ((etcId.length()) / 4),
//                    UHfData.UHfGetData.hexStringToBytes(etcId), (byte) 1, word,
//                    UHfData.UHfGetData.hexStringToBytes(contentStr),
//                    pwdBytes);
//            if (result != 0) {
//                Toast.makeText(ScanActivity.this, "亮灯失败", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(ScanActivity.this, "亮灯成功", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    private void showData() {
        data.clear();
        for (String s : CARDS) {
            UHfData.InventoryTagMap itm = new UHfData.InventoryTagMap();
            itm.strEPC = s;
            data.add(itm);
        }
        scanAdapter.setData(data);
    }


    private void signageManagementDetail(final String epc) {
        OkGo.<String>post(UrlUtils.TEST_URL + UrlUtils.SIGNBOARDDETAIL)
                .tag(this)
                .headers("token", (String) SharedPreferencesUtil.get(this, ParamUtil.TOKEN, ""))
                .params("epc", epc)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        hideProgressLoading();
                        SignageManagementBean signageManagementBean = new Gson().fromJson(response.body(), SignageManagementBean.class);
                        if (signageManagementBean.getMsg().equals(UrlUtils.METHOD_POST_SUCCESS)) {
                            String pwd = signageManagementBean.getSignBoard().getPassword();
                            lighting(epc, pwd);
                        } else {
                            Toast.makeText(ScanActivity.this, signageManagementBean.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        hideProgressLoading();
                        Toast.makeText(ScanActivity.this, "请求错误！" + response.code() + "-------" + response.message(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
