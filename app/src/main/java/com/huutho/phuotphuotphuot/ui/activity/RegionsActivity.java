package com.huutho.phuotphuotphuot.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.huutho.phuotphuotphuot.R;
import com.huutho.phuotphuotphuot.base.activity.BaseActivity;
import com.huutho.phuotphuotphuot.base.entity.BaseEntity;
import com.huutho.phuotphuotphuot.ui.adapter.ChooseCityAdapter;
import com.huutho.phuotphuotphuot.ui.adapter.PlaceRVAdapter;
import com.huutho.phuotphuotphuot.ui.entity.City;
import com.huutho.phuotphuotphuot.ui.entity.Place;
import com.huutho.phuotphuotphuot.ui.fragment.InformationFragment;
import com.huutho.phuotphuotphuot.ui.fragment.ReportForMeFragment;
import com.huutho.phuotphuotphuot.ui.fragment.UpdateFragment;
import com.huutho.phuotphuotphuot.utils.database.DbContracts;
import com.huutho.phuotphuotphuot.utils.database.TablePlace;
import com.huutho.phuotphuotphuot.widget.ClearEditText;
import com.huutho.phuotphuotphuot.widget.TextViewRoboto;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by HuuTho on 1/21/2017.
 */
public class RegionsActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        PlaceRVAdapter.IPlaceAdapterListener, View.OnClickListener {
    protected static final String KEY_BUNDLE_REGIONS = "key.bundle.regions";

    private static final int KEY_RESULT_SPEECH = 1010;
    protected static final int SNUMBER_COLUMN = 2;
    protected static final int REGIONS_DEFAULT = 0;
    protected static final int REGIONS_NORTH = 1;
    protected static final int REGIONS_CENTRAL = 2;
    protected static final int REGIONS_SOUTH = 3;

    @BindView(R.id.atc_regions_drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.act_regions_navigation_view)
    NavigationView mNavView;
    @BindView(R.id.act_regions_toobar)
    Toolbar mToobar;
    @BindView(R.id.act_regions_toolbar_title)
    TextViewRoboto mToolbarTitle;
    @BindView(R.id.act_regions_choose_city)
    ImageView mChooseLocation;
    @BindView(R.id.act_home_edt_search)
    ClearEditText mEdtSearch;
    @BindView(R.id.act_regions_rv_place)
    RecyclerView mRVPlace;

    private ActionBarDrawerToggle mToggle;
    private PlaceRVAdapter mAdapter;
    private ArrayList<Place> mListPlace;
    private AlertDialog mSpeechDialog;
    private int mCurrentRegions;
    private City mCurrentCity;
    private City planCity;

    private Runnable runActivityReady = new Runnable() {
        @Override
        public void run() {
            mListPlace = new ArrayList<>();
            mListPlace.addAll(TablePlace.getInstance().getListData(
                    DbContracts.TablePlace.PLACE_ID_ZONE,
                    new String[]{String.valueOf(mCurrentRegions)},
                    null));
            mAdapter.setDatas(mListPlace);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCurrentRegions = getDataBundle();
        super.onCreate(savedInstanceState);
    }

    @Override
    public int setContentLayout() {
        return R.layout.activity_regions;
    }

    @Override
    public void bindViewToLayout() {
        ButterKnife.bind(this);

        // setup navigationview
        mNavView.setItemIconTintList(null);
        mNavView.setNavigationItemSelectedListener(this);
        removeScrollbarNavigationView(mNavView);


        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToobar, R.string.open, R.string.close);
        mToggle.syncState();
        mDrawerLayout.addDrawerListener(mToggle);

        mAdapter = new PlaceRVAdapter(this, this);
        mRVPlace.setLayoutManager(new GridLayoutManager(this, SNUMBER_COLUMN));
        mRVPlace.hasFixedSize();

        mRVPlace.setAdapter(mAdapter);
        mChooseLocation.setOnClickListener(this);
        mEdtSearch.addTextChangedListener(txtWatcher);

        // set font roboto for edittext
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        mEdtSearch.setTypeface(typeface);
        mEdtSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // set font roboto for title toolbar
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("PhuotPhuotPhuot");
    }


    @Override
    public void activityReady() {
        dialogSpeech();
        getHandler().post(runActivityReady);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_regions_choose_city:
                dialogChooseCity();
                break;
        }
    }

    @Override
    public void onRecyclerViewItemClick(Place dataItem, View view, int position) {
        Intent intent = new Intent(RegionsActivity.this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.KEY_DATA, dataItem);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        getHandler().post(runCloseNav);
        switch (item.getItemId()) {
            case R.id.action_vietnam:
                mCurrentRegions = REGIONS_DEFAULT;
                notifyDataRegions(mCurrentRegions);
                break;

            case R.id.action_voice_search:
                mSpeechDialog.show();
                break;

            case R.id.action_favorite:
                notifyDataFavorite();
                break;

            case R.id.action_north:
                mCurrentRegions = REGIONS_NORTH;
                notifyDataRegions(mCurrentRegions);
                break;

            case R.id.action_central:
                mCurrentRegions = REGIONS_CENTRAL;
                notifyDataRegions(mCurrentRegions);
                break;

            case R.id.action_south:
                mCurrentRegions = REGIONS_SOUTH;
                notifyDataRegions(mCurrentRegions);
                break;

            case R.id.action_flash_light:
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        FlashLightActivity.lauch(RegionsActivity.this);
                    }
                });
                break;

            case R.id.action_experiences:
                ExperienceTravelActivity.newInstance(this);
                break;

            case R.id.action_sos:
                startActivity(new Intent(RegionsActivity.this, SOSActivity.class));
                break;

            case R.id.action_update:
                goToUpdateFragment();
                break;

            case R.id.action_info:
                goToInfomationFragment();
                break;
        }
        item.setChecked(true);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == KEY_RESULT_SPEECH && data != null) {
            mSpeechDialog.dismiss();
            ArrayList<String> myText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mEdtSearch.setText(myText.get(0));
            mEdtSearch.requestApplyInsets();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_regions_activity_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                mToggle.onOptionsItemSelected(item);
                return true;

            case R.id.action_grid_list:
                item.setChecked(!item.isChecked());
                if (!item.isChecked()) {
                    mRVPlace.setLayoutManager(new GridLayoutManager(RegionsActivity.this, 2));
                    mAdapter.setType(PlaceRVAdapter.TYPE_GRID);
                    item.setIcon(R.drawable.ic_view_grid);
                } else {
                    mRVPlace.setLayoutManager(new LinearLayoutManager(RegionsActivity.this));
                    mAdapter.setType(PlaceRVAdapter.TYPE_LIST);
                    item.setIcon(R.drawable.ic_view_list);
                }
                return true;
            case R.id.action_setting_dots:
                return true;
            case R.id.action_setting:
                xxxxxSetting();
                return true;
            case R.id.action_share:
                xxxxxShareOnClick();
                return true;
            case R.id.action_report:
                xxxxxReportForMe();
                return true;
            case R.id.action_update:
                goToUpdateFragment();
                return true;
            case R.id.action_info:
                goToInfomationFragment();
                return true;



        }
        return true;
    }

    private void xxxxxSetting() {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
    }

    private void xxxxxShareOnClick() {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Mời bạn tải ứng dụng Phượt tốt nhất hiện nay");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://playstore/?id=com.huutho.phuotphuotphuot");

        startActivity(Intent.createChooser(shareIntent, "Mời chọn phương tiện chia sẻ =]]"));
    }

    private void xxxxxReportForMe() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                setFragment(new ReportForMeFragment());
            }
        });
    }


    /*----------------------------------------------*/

    private void dialogSpeech() {
        mSpeechDialog = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.
                from(RegionsActivity.this).
                inflate(R.layout.dialog_speech_to_text, null);
        mSpeechDialog.setView(view);

        view.findViewById(R.id.dialog_speech_to_text_imv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.title_dialog_speech_to_text);
                try {
                    startActivityForResult(intent, KEY_RESULT_SPEECH);
                } catch (Exception e) {
                    Toast.makeText(RegionsActivity.this, R.string.toast_not_support_speech, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dialogChooseCity() {

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_choose_city, null);
        AlertDialog.Builder mChooseCityDialog = new AlertDialog.Builder(this);
        mChooseCityDialog.setTitle(this.getResources().getString(R.string.title_dialog_choose_city));
        mChooseCityDialog.setView(view);

        RecyclerView viewCities = (RecyclerView) view.findViewById(R.id.rv_choose_city);
        viewCities.setLayoutManager(new LinearLayoutManager(this));
        viewCities.setAdapter(new ChooseCityAdapter(this, mCurrentRegions, new ChooseCityAdapter.ICitySelected() {
            @Override
            public void onRecyclerViewItemClick(BaseEntity dataItem, View view, int position) {
                planCity = (City) dataItem;
            }
        }));

        mChooseCityDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentCity = planCity;
                notifyDataCity(mCurrentCity);
            }
        });

        mChooseCityDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mChooseCityDialog.show();
    }

    /*----------------------------------------------*/

    private int getDataBundle() {
        Intent intent = getIntent();
        return intent.getIntExtra(KEY_BUNDLE_REGIONS, REGIONS_DEFAULT);
    }

    private void notifyDataRegions(int regions) {
        if (regions == REGIONS_DEFAULT) {
            mAdapter.setDatas(TablePlace.getInstance().getListData(null, null, null));
            return;
        }
        mAdapter.setDatas(TablePlace.getInstance()
                .getListData(
                        DbContracts.TablePlace.PLACE_ID_ZONE,
                        new String[]{String.valueOf(regions)},
                        null));
    }

    private void notifyDataCity(City city) {
        try {
            mAdapter.setDatas(TablePlace.getInstance().getListData(
                    DbContracts.TablePlace.PLACE_ID_CITY,
                    new String[]{city.getmIdCity()},
                    null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyDataSearch(String query) {
        mAdapter.setDatas(TablePlace.getInstance()
                .getListData(
                        DbContracts.TablePlace.PLACE_NAME_PLACE + " LIKE '%" + query + "%' ",
                        null,
                        null));
    }

    private void notifyDataFavorite() {
        mAdapter.setDatas(TablePlace.getInstance()
                .getListData(DbContracts.TablePlace.PLACE_FAVORITE, new String[]{PlaceRVAdapter.FAV}, null));
    }

    private void removeScrollbarNavigationView(NavigationView navView) {
        if (navView != null) {
            NavigationMenuView menuView = (NavigationMenuView) navView.getChildAt(0);
            if (menuView != null) {
                menuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    private void setFragment(Fragment fragment) {
        addFragmentAnim(R.id.atc_regions_drawer_layout, fragment);
    }

    private void goToUpdateFragment() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                setFragment(new UpdateFragment());
            }
        });
    }

    private void goToInfomationFragment() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                setFragment(new InformationFragment());
            }
        });
    }


    private Runnable runCloseNav = new Runnable() {
        @Override
        public void run() {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    };

    private TextWatcher txtWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            if (start == 0) {
                if (mCurrentCity != null) {
                    notifyDataCity(mCurrentCity);
                } else {
                    notifyDataRegions(mCurrentRegions);
                }
                return;
            }
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getHandler().removeCallbacks(this);
                    notifyDataSearch(String.valueOf(s));
                }
            }, 1000);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onBackPressed() {
       if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
           mDrawerLayout.closeDrawer(GravityCompat.START);
       }else {
           super.onBackPressed();
       }
    }
}
