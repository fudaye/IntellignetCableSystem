package com.system.intellignetcable.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.system.intellignetcable.R;
import com.system.intellignetcable.util.IntentHelper;
import com.system.intellignetcable.util.ParamUtil;
import com.system.intellignetcable.util.SharedPreferencesUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by fudaye on 2019/1/28.
 * <p>
 * 首页工单分类界面
 */


public class MainFragment extends BaseFragment {

    private static MainFragment sMainFragment;
    @BindView(R.id.ll_unexecuted)
    LinearLayout llUnexecuted;
    @BindView(R.id.search_et)
    EditText mEditText;
    @BindView(R.id.ll_submit)
    LinearLayout mLlSubmit;

    private Unbinder mUnbinder;

    public static MainFragment getInstance() {
        if (sMainFragment == null) {
            sMainFragment = new MainFragment();
        }
        return sMainFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmeng_main, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        int type = (int) SharedPreferencesUtil.get(getActivity(), ParamUtil.TYPE, 2);
        if (type == 1) {
            llUnexecuted.setVisibility(View.GONE);
            mLlSubmit.setVisibility(View.GONE);
        }
        mEditText.setCursorVisible(false);
        mEditText.setFocusable(false);
        mEditText.setFocusableInTouchMode(false);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick({R.id.ll_unexecuted, R.id.ll_check, R.id.ll_rejected, R.id.ll_finish, R.id.search_et, R.id.scan_rl, R.id.title_bar, R.id.ll_submit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_unexecuted:
                IntentHelper.jumpOrderListActivity(getActivity(), 0);
                break;
            case R.id.ll_submit:
                IntentHelper.jumpOrderListActivity(getActivity(), 1);
                break;
            case R.id.ll_check:
                IntentHelper.jumpOrderListActivity(getActivity(), 2);
                break;
            case R.id.ll_rejected:
                IntentHelper.jumpOrderListActivity(getActivity(), 4);
                break;
            case R.id.ll_finish:
                IntentHelper.jumpOrderListActivity(getActivity(), 3);
                break;
            case R.id.search_et:
            case R.id.title_bar:
                IntentHelper.jumpOrderListActivity(getActivity(), -1);
                break;
            case R.id.scan_rl:
                IntentHelper.jumpScanActivity(getActivity(), false);
                break;

        }
    }
}
