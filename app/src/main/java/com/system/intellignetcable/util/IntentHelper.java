package com.system.intellignetcable.util;

import android.content.Context;
import android.content.Intent;

import com.jph.takephoto.model.TImage;
import com.system.intellignetcable.activity.OrderListActivity;
import com.system.intellignetcable.activity.PhotoViewActivity;
import com.system.intellignetcable.activity.ScanActivity;

import java.util.ArrayList;

/**
 * Created by fudaye on 2019/1/28.
 */

public class IntentHelper {

    public static void jumpOrderListActivity(Context context, int status) {
        Intent intent = new Intent(context, OrderListActivity.class);
        intent.putExtra("status", status);
        context.startActivity(intent);
    }

    public static void jumpScanActivity(Context context, boolean isEdit, int workId) {
        Intent intent = new Intent(context, ScanActivity.class);
        intent.putExtra("isEdit", isEdit);
        intent.putExtra("workId", workId);
        context.startActivity(intent);
    }

    public static void jumpScanActivity(Context context, boolean isEdit) {
        jumpScanActivity(context, isEdit, -1);
    }

    public static void jumpPhotoViewActivity(Context context, ArrayList<TImage> url , int  position) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("position",position);
        context.startActivity(intent);
    }
}
