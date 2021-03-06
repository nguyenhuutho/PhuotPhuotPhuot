package com.huutho.phuotphuotphuot.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.huutho.phuotphuotphuot.R;
import com.huutho.phuotphuotphuot.app.AppController;
import com.huutho.phuotphuotphuot.base.activity.BaseActivity;
import com.huutho.phuotphuotphuot.utils.ImageUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by HuuTho on 1/19/2017.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private int blocWidth;
    private int blocHeight;

    @BindView(R.id.act_home_image_vietnam)
    ImageView imgVietNam;

    int countTimeBackPressSecodeTime;
    boolean isExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    public int setContentLayout() {
        return R.layout.activity_home;
    }

    @Override
    public void bindViewToLayout() {
        ButterKnife.bind(this);
    }

    @Override
    public void activityReady() {
        blocWidth = AppController.WIDTH_SCREEN / 16;
        blocHeight = AppController.HEIGHT_SCREEN / 24;
        ImageUtils.loadImageResource(this, R.drawable.background_vietnam, imgVietNam);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();


        if (action == MotionEvent.ACTION_DOWN) {

            if (x > 1 * blocWidth && x < 8 * blocWidth && y > 2 * blocHeight && y < 6 * blocHeight) {
                startActivity(RegionsActivity.REGIONS_NORTH);
                return false;
            }

            if (x > 3 * blocWidth && x < 11 * blocWidth && y > 6 * blocHeight && y < 16 * blocHeight) {
                startActivity(RegionsActivity.REGIONS_CENTRAL);
                return false;
            }

            if (x > 6 * blocWidth && x < 11 * blocWidth && y > 6 * blocHeight && y < 22 * blocHeight) {
                startActivity(RegionsActivity.REGIONS_SOUTH);
                return false;
            }
        }

        return true;
    }

    private void startActivity(int regions) {
        Intent intent = new Intent(HomeActivity.this, RegionsActivity.class);
        intent.putExtra(RegionsActivity.KEY_BUNDLE_REGIONS, regions);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (isExit){
            super.onBackPressed();
        }else {
            Toast.makeText(this,"Ấn lại để thoát",Toast.LENGTH_SHORT).show();
            isExit = true;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isExit = false;
            }
        }, 1500);
    }
}
