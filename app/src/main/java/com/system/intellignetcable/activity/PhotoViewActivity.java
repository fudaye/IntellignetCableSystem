package com.system.intellignetcable.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jph.takephoto.model.TImage;
import com.system.intellignetcable.R;
import com.system.intellignetcable.view.HackyViewPager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by fudaye on 2017/10/23.
 */

public class PhotoViewActivity extends BaseActivity {

    @BindView(R.id.title_tv)
    TextView mTitleTv;
    @BindView(R.id.view_pager)
    HackyViewPager mViewPager;
    private ArrayList<TImage> urls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ButterKnife.bind(this);
        mTitleTv.setText("图片浏览");
        urls = (ArrayList<TImage>) getIntent().getSerializableExtra("url");
        mViewPager.setAdapter(new SamplePagerAdapter());
    }

    private class SamplePagerAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(PhotoViewActivity.this).inflate(R.layout.vp_image_view, null);
            PhotoView photoView = (PhotoView) view.findViewById(R.id.photoview);
            Glide.with(container.getContext()).load(urls.get(position).getOriginalPath()).into(photoView);
            // Now just add PhotoView to ViewPager and return it
            TextView tv = (TextView) view.findViewById(R.id.tv_image_size);
            position += 1;
            tv.setText(position + "/" + urls.size());
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
