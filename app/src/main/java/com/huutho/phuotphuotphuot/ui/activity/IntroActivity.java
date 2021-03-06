package com.huutho.phuotphuotphuot.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.huutho.phuotphuotphuot.R;
import com.huutho.phuotphuotphuot.app.Config;
import com.huutho.phuotphuotphuot.base.activity.BaseActivity;
import com.huutho.phuotphuotphuot.ui.adapter.IntroPagerAdapter;
import com.huutho.phuotphuotphuot.ui.entity.IntroItem;
import com.huutho.phuotphuotphuot.utils.FileUtils;
import com.huutho.phuotphuotphuot.utils.SharePreferencesUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;

public class IntroActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.act_intro_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.act_intro_btn_next)
    Button mButtonNext;
    @BindView(R.id.act_intro_btn_previos)
    Button mButtomPrevious;
    @BindView(R.id.indicator)
    CircleIndicator mIndicator;

    private IntroPagerAdapter mAdapter;
    private ArrayList<IntroItem> introItems;
    int mLastPositionPage = 0;
    int currentPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        boolean isFirstLoad = SharePreferencesUtils.getInstances().getFirstRunApp();
        if (isFirstLoad) {
            getHandler().post(runFirstLoad);
        } else {
            startActivity(new Intent(IntroActivity.this, HomeActivity.class));
            finish();
        }
    }

    private Runnable runFirstLoad = new Runnable() {
        @Override
        public void run() {
            SharePreferencesUtils.getInstances().setFirstRunApp(false);
            FileUtils.copyDatabase(Config.PATH_DB);
        }
    };

    @Override
    public int setContentLayout() {
        return R.layout.activity_intro;
    }

    @Override
    public void bindViewToLayout() {
        ButterKnife.bind(this);
        mButtonNext.setOnClickListener(this);
        mButtomPrevious.setOnClickListener(this);
        mButtomPrevious.setVisibility(View.INVISIBLE);

    }

    @Override
    public void activityReady() {
        introItems = new ArrayList<>();
        introItems.add(new IntroItem("Giao diện thân thiện, đẹp mắt", R.drawable.intro1));
        introItems.add(new IntroItem("Menu đa dạng, dễ sử dụng", R.drawable.intro2));
        introItems.add(new IntroItem("Bản đồ chi tiết đến từng cung đường", R.drawable.intro3));
        introItems.add(new IntroItem("Hình ảnh da dạng", R.drawable.intro4));
        introItems.add(new IntroItem("Thông tin hữu ích cho bạn", R.drawable.intro5));

        mAdapter = new IntroPagerAdapter(introItems);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        mViewPager.setOffscreenPageLimit(mAdapter.getCount());
        mIndicator.setViewPager(mViewPager);
    }

    /**
     * Activity click event
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        currentPosition = mViewPager.getCurrentItem();
        switch (v.getId()) {
            case R.id.act_intro_btn_next:
                if (currentPosition == mAdapter.getCount()-1) {
                    startActivity(new Intent(IntroActivity.this, HomeActivity.class));
                    finish();
                } else {
                    mLastPositionPage = currentPosition;
                    currentPosition++;
                    mViewPager.setCurrentItem(currentPosition);

                }
                break;
            case R.id.act_intro_btn_previos:
                mLastPositionPage = currentPosition;
                currentPosition--;
                mViewPager.setCurrentItem(currentPosition);
                break;
        }
    }

    /**
     * ViewPager Event PagerChangedListener
     */

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {


            if (position == mAdapter.getCount()-1) {
                mButtonNext.setText("Let's start");
                mButtomPrevious.setVisibility(View.INVISIBLE);
            } else if (position == 0) {
                mButtomPrevious.setVisibility(View.INVISIBLE);
            } else {
                mButtonNext.setText("Next");
                if (mButtomPrevious.getVisibility() == View.INVISIBLE)
                    mButtomPrevious.setVisibility(View.VISIBLE);
            }
            currentPosition = position;
            mLastPositionPage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };
}
