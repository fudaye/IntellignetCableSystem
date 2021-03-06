package com.system.intellignetcable.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.system.intellignetcable.R;
import com.system.intellignetcable.activity.DispatchSheetActivity;
import com.system.intellignetcable.activity.OrderInfoDetailActivity;
import com.system.intellignetcable.activity.OrderListActivity;
import com.system.intellignetcable.adapter.NewOrderAdapter;
import com.system.intellignetcable.adapter.StringListAdapter;
import com.system.intellignetcable.bean.OrderListBean;
import com.system.intellignetcable.util.IntentHelper;
import com.system.intellignetcable.util.ParamUtil;
import com.system.intellignetcable.util.SharedPreferencesUtil;
import com.system.intellignetcable.util.UrlUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NewOrderFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnRefreshListener, OnLoadMoreListener {
    private static final String TAG = "NewOrderFragment";
    @BindView(R.id.search_iv)
    ImageView searchIv;
    @BindView(R.id.search_et)
    EditText searchEt;
    @BindView(R.id.search_rl)
    RelativeLayout searchRl;
    @BindView(R.id.order_list)
    ListView orderList;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.add_iv)
    ImageView addIv;
    @BindView(R.id.dispatch_sheet_rl)
    RelativeLayout dispatchSheetRl;
    @BindView(R.id.hint_tv)
    TextView hintTv;


    Unbinder unbinder;
    private static NewOrderFragment newOrderFragment;
    //    @BindView(R.id.fab)
//    Button fab;
    @BindView(R.id.search_lv)
    ListView searchLv;
    private OrderListActivity mainActivity;
    private int mType;
    private int pageIndex = 1;
    private int pageSize = 10;
    private Gson gson;
    private List<OrderListBean.PageBean.ListBean> list;
    private int mUserId;
    private NewOrderAdapter newOrderAdapter;
    private StringListAdapter adapter;
    private String mWorkAddress = "";

    public static NewOrderFragment getInstance() {
        if (newOrderFragment == null) {
            newOrderFragment = new NewOrderFragment();
        }
        return newOrderFragment;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mainActivity = (OrderListActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_new, container, false);
        unbinder = ButterKnife.bind(this, view);
        setLoadingView(hintTv, refreshLayout);
        initData();
        setListener();
        String s = "搜索";//0未执行 1 正在提交 2 待审核/已提交 3 已完成 4 已驳回
        switch (mainActivity.getStatus()) {
            case 0:
                s += "未执行工单";
                break;
            case 1:
                s += "正在提交工单";
                break;
            case 2:
                s += "待审核工单";
                break;
            case 3:
                s += "已完成工单";
                break;
            case 4:
                s += "已驳回工单";
                break;
        }

        searchEt.setHint(s);
        return view;
    }

    private void initData() {
        showLoading();
        refreshLayout.setRefreshHeader(new MaterialHeader(mainActivity).setShowBezierWave(false));
        refreshLayout.setRefreshFooter(new ClassicsFooter(mainActivity));
        gson = new Gson();
        mType = (int) SharedPreferencesUtil.get(mainActivity, ParamUtil.TYPE, 2);
        mUserId = (int) SharedPreferencesUtil.get(mainActivity, ParamUtil.USER_ID, 0);
        list = new ArrayList<>();
//        if (type == 1) {
//            dispatchSheetRl.setVisibility(View.VISIBLE);
//            fab.setVisibility(View.GONE);
//        } else {
//            dispatchSheetRl.setVisibility(View.GONE);
//            fab.setVisibility(View.VISIBLE);
//        }
        getOrderList(mUserId, mType, String.valueOf(pageIndex), String.valueOf(pageSize), mainActivity.getStatus(), mWorkAddress);
        newOrderAdapter = new NewOrderAdapter(getActivity(), list);
        orderList.setAdapter(newOrderAdapter);
    }

    private void setListener() {
        orderList.setOnItemClickListener(this);
        refreshLayout.setOnLoadMoreListener(this);
        refreshLayout.setOnRefreshListener(this);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mWorkAddress = s.toString();
                if (TextUtils.isEmpty(s.toString())) {
                    mainActivity.hideSoftInput(searchEt);
                    searchLv.setVisibility(View.GONE);
                    list.clear();
                    pageIndex = 1;
                    getOrderList(mUserId, mType, String.valueOf(pageIndex), String.valueOf(pageSize), mainActivity.getStatus(), mWorkAddress);
                } else {
                    searchList(mUserId, mType, s.toString(), "1", "10");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.search_iv)
    public void onSearchIvClicked() {
//        if (!searchEt.getText().toString().isEmpty()) {
//            Intent intent = new Intent(mainActivity, OrderSearchResultActivity.class);
//            intent.putExtra(ParamUtil.SEARCH_CONTENT, searchEt.getText().toString());
//            startActivity(intent);
//            searchEt.setText("");
//        }
    }

    @OnClick(R.id.dispatch_sheet_rl)
    public void onDispatchSheetRlClicked() {
        startActivity(new Intent(mainActivity, DispatchSheetActivity.class));
    }

    private void getOrderList(int userId, int type, String pageNo, String pageSize, int status, String workAddress) {
        String url;
        if (status == -1) {
            url = UrlUtils.TEST_URL + UrlUtils.METHOD_POST_WORK_ORDER_LIST + "?userId=" + userId + "&type=" + type + "&pageNo=" + pageNo + "&pageSize=" + pageSize + "&workAddress=" + workAddress;
        } else {
            url = UrlUtils.TEST_URL + UrlUtils.METHOD_POST_WORK_ORDER_LIST + "?userId=" + userId + "&type=" + type + "&pageNo=" + pageNo + "&pageSize=" + pageSize + "&status=" + status + "&workAddress=" + workAddress;
        }
        OkGo.<String>post(url)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        refreshLayout.finishLoadMore();
                        refreshLayout.finishRefresh();
                        OrderListBean orderListBean = gson.fromJson(response.body(), OrderListBean.class);
                        if (orderListBean.getMsg().equals(UrlUtils.METHOD_POST_SUCCESS)) {
                            if (orderListBean.getPage().getList() == null || orderListBean.getPage().getList().size() == 0) {
                                showNoData();
                            } else if (orderListBean.getPage().getList() != null && orderListBean.getPage().getList().size() > 0) {
                                showDataSuc();
                                list.addAll(orderListBean.getPage().getList());
                                newOrderAdapter.notifyDataSetChanged();
                            }
                            if (orderListBean.getPage().getList() == null || orderListBean.getPage().getList().size() < 10) {
                                refreshLayout.setEnableLoadMore(false);
                            } else {
                                refreshLayout.setEnableLoadMore(true);
                            }
                        } else {
                            showFail();
                            Toast.makeText(getActivity(), orderListBean.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        showFail();
                        refreshLayout.finishLoadMore();
                        refreshLayout.finishRefresh();
                        Toast.makeText(getActivity(), "请求错误！", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "请求错误：" + response.code() + "-------" + response.message());
                    }
                });
    }

    private void searchList(int userId, int type, String workAddress, String pageNo, final String pageSize) {
        OkGo.<String>post(UrlUtils.TEST_URL + UrlUtils.METHOD_POST_WORK_ORDER_LIST + "?userId=" + userId + "&type=" + type + "&workAddress=" + workAddress + "&pageNo=" + pageNo + "&pageSize=" + pageSize)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        final OrderListBean orderListBean = gson.fromJson(response.body(), OrderListBean.class);
                        if (orderListBean.getMsg().equals(UrlUtils.METHOD_POST_SUCCESS)) {
                            if (orderListBean.getPage().getList() == null || orderListBean.getPage().getList().size() == 0) {
                                searchLv.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), "没有搜到相关数据！", Toast.LENGTH_SHORT).show();
                            } else if (orderListBean.getPage().getList().size() > 0) {
                                final List<String> strings = new ArrayList<>();
                                searchLv.setVisibility(View.VISIBLE);
                                for (int i = 0; i < orderListBean.getPage().getList().size(); i++) {
                                    strings.add(orderListBean.getPage().getList().get(i).getWorkAddress());
                                }
                                adapter = new StringListAdapter(getActivity(), strings);
                                searchLv.setAdapter(adapter);
                                searchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        mainActivity.hideSoftInput(view);
                                        String keyWord = strings.get(position);
                                        searchEt.setText(keyWord);
                                        mWorkAddress = keyWord;
                                        list.clear();
//                                        getOrderList(mUserId, mType, String.valueOf(1), String.valueOf(10), mainActivity.getStatus(), keyWord);
//                                        Intent intent = new Intent(mainActivity, OrderInfoDetailActivity.class);
//                                        intent.putExtra(ParamUtil.WORK_ORDER_ID, orderListBean.getPage().getList().get(position).getWorkOrderId());
//                                        startActivity(intent);
                                    }
                                });
                                list.clear();
                                getOrderList(mUserId, mType, String.valueOf(1), String.valueOf(10), mainActivity.getStatus(), mWorkAddress);
                            }
                        } else {
                            showFail();
                            Toast.makeText(getActivity(), orderListBean.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        showFail();
                        Toast.makeText(getActivity(), "请求错误！", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "请求错误：" + response.code() + "-------" + response.message());
                    }
                });
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        pageIndex++;
        getOrderList(mUserId, mType, String.valueOf(pageIndex), String.valueOf(pageSize), mainActivity.getStatus(), mWorkAddress);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mWorkAddress = "";
        list.clear();
        pageIndex = 1;
        getOrderList(mUserId, mType, String.valueOf(pageIndex), String.valueOf(pageSize), mainActivity.getStatus(), mWorkAddress);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainActivity.hideSoftInput(view);
        Intent intent = new Intent(mainActivity, OrderInfoDetailActivity.class);
        intent.putExtra(ParamUtil.WORK_ORDER_ID, list.get(position).getWorkOrderId());
        intent.putExtra("status", mainActivity.getStatus());
        startActivity(intent);
    }

    @OnClick(R.id.scan_rl)
    public void onViewClicked() {
        IntentHelper.jumpScanActivity(getActivity(), false);
    }
}
