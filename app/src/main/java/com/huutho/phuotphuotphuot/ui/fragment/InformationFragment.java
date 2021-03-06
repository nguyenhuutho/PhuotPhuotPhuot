package com.huutho.phuotphuotphuot.ui.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.huutho.phuotphuotphuot.R;
import com.huutho.phuotphuotphuot.base.fragment.BaseFragment;
import com.huutho.phuotphuotphuot.widget.ToobarBackButton;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nguyenhuutho on 2/1/17.
 */

public class InformationFragment extends BaseFragment {

    private String info = "Ứng dụng Phượt trên Android version 1.0 " + "\n"
            + "Nhóm thực hiện gồm các thành viên : "
            + "\n" + "\t- Nguyễn Hữu Thọ"
            + "\n" + "\t- Ngô Nguyễn Tử Anh"
            + "\n" + "\t- Trần Thị Hà"
            +"\n" + "Mọi thắc mắc, và thông tin chi tiết xin liên hệ : singgumcole@gmail.com";

    @BindView(R.id.fragment_information_toobar_back_button)
    ToobarBackButton mToobar;
    @BindView(R.id.fragment_information_txt_info)
    AppCompatTextView mTextInformation;

    @Override
    public int setContentLayout() {
        return R.layout.fragment_information;
    }

    @Override
    public void bindViewToFragment(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, getView());
        mToobar.setTitle(R.string.title_fragment_about);
        mToobar.setTitleTextColor(ContextCompat.getColor(mContext, R.color.white));
        mTextInformation.setText(info);
    }

    @Override
    public void fragmentReady() {

    }
}
