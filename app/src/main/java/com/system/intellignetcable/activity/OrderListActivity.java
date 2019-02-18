package com.system.intellignetcable.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.system.intellignetcable.R;

/**
 * Created by fudaye on 2019/1/28.
 */

public class OrderListActivity extends BaseActivity {

    private int mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatus = getIntent().getIntExtra("status", 0);
        setContentView(R.layout.activity_order_list);
    }

    public int getStatus() {
        return mStatus;
    }
}
