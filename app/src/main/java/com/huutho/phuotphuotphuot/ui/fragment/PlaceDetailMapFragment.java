package com.huutho.phuotphuotphuot.ui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.huutho.phuotphuotphuot.R;
import com.huutho.phuotphuotphuot.app.retrofit.ApiRequest;
import com.huutho.phuotphuotphuot.app.retrofit.ApiRequestHelper;
import com.huutho.phuotphuotphuot.base.adapter.IBaseAdapterCallback;
import com.huutho.phuotphuotphuot.base.entity.BaseEntity;
import com.huutho.phuotphuotphuot.base.fragment.MapFragment;
import com.huutho.phuotphuotphuot.location.AnalyzeSteps;
import com.huutho.phuotphuotphuot.location.RoutesLocation;
import com.huutho.phuotphuotphuot.ui.adapter.NavigateDirectionAdapter;
import com.huutho.phuotphuotphuot.ui.entity.Place;
import com.huutho.phuotphuotphuot.ui.entity.PlaceRested;
import com.huutho.phuotphuotphuot.utils.MapUtils;
import com.huutho.phuotphuotphuot.utils.SharePreferencesUtils;
import com.huutho.phuotphuotphuot.utils.database.DbContracts;
import com.huutho.phuotphuotphuot.utils.database.TableRested;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by HuuTho on 2/16/2017.
 */

public class PlaceDetailMapFragment extends MapFragment implements OnMapReadyCallback, Callback<RoutesLocation>, IBaseAdapterCallback {
    private static final String EXTRA_MAP_FRAGMENT = "extra.map.fragment";

    private static final int REQUEST_PERMISSION = 111;
    private Place mPlace;
    private ArrayList<PlaceRested> mResteds;
    private MapView mGoogleMap;
    private GoogleMap mMap;
    private RoutesLocation routesLocation;
    private ArrayList<RoutesLocation.RoutesBean.LegsBean.StepsBean> mListDirectionDetailInfo = new ArrayList<>();

    private NavigateDirectionAdapter navigateDirectionAdapter;

    RecyclerView rvNavigate;


    @BindView(R.id.imv_draw_map)
    public ImageView mImvDrawOnMap;

    @BindView(R.id.imv_navigate)
    public ImageView mImvNavigate;

    @BindView(R.id.txt_detail_direction)
    public TextView mTxtDetailDirection;

    public static PlaceDetailMapFragment newInstance(Place place) {
        Bundle args = new Bundle();
        PlaceDetailMapFragment fragment = new PlaceDetailMapFragment();
        args.putParcelable(EXTRA_MAP_FRAGMENT, place);
        fragment.setArguments(args);
        return fragment;
    }

    private Place getBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return getArguments().getParcelable(EXTRA_MAP_FRAGMENT);
        }
        return savedInstanceState.getParcelable(EXTRA_MAP_FRAGMENT);
    }

    @Override
    public int setContentLayout() {
        return R.layout.fragment_place_detail_map;
    }

    @Override
    public void bindViewToFragment(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, getView());
        mResteds = new ArrayList<>();
        mPlace = getBundleData(savedInstanceState);
        mGoogleMap = (MapView) view.findViewById(R.id.map);
        getHandle().post(runLoadMap);
        getHandle().post(runLoadMotelLocation);

    }

    @Override
    public void fragmentReady() {

        mImvDrawOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (routesLocation != null)
                    drawDirectionOnMap(routesLocation);
                else {
                    String destination = MapUtils.convertStandStrLocation(mPlace.mLatLng);
                    String origin = SharePreferencesUtils.getInstances().getLastKnowLocation();

                    Retrofit retrofit = new ApiRequestHelper().getMapApiRequest();
                    ApiRequest request = retrofit.create(ApiRequest.class);
                    Call<RoutesLocation> call = request.getDirection(origin, destination, ApiRequest.MODE_DRIVING, ApiRequest.LANGUAGE);
                    call.enqueue(PlaceDetailMapFragment.this);
                }
            }
        });


        mImvNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = LayoutInflater.from(mActivity).inflate(R.layout.layout_navigate_direction, null);
                rvNavigate = (RecyclerView) view.findViewById(R.id.rv_navigate);
                rvNavigate.setLayoutManager(new LinearLayoutManager(mContext));
                if (navigateDirectionAdapter != null) rvNavigate.setAdapter(navigateDirectionAdapter);

                new AlertDialog.Builder(mContext)
                        .setView(view)
                        .setTitle("Navigate Detail")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
    }

    @Override
    public void onLocationChange(String location) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MapsInitializer.initialize(mContext);

        if (checkPermission()) {
            googleMap.setMyLocationEnabled(true);
            settingUiMap(mMap);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // try again, explain why we need this permission
                setDialogShowRequestPermission();
            } else {
                // direct or tick remember
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
            }
        }

        String destination = MapUtils.convertStandStrLocation(mPlace.mLatLng);
        String origin = SharePreferencesUtils.getInstances().getLastKnowLocation();

        Retrofit retrofit = new ApiRequestHelper().getMapApiRequest();
        ApiRequest request = retrofit.create(ApiRequest.class);
        Call<RoutesLocation> call = request.getDirection(origin, destination, ApiRequest.MODE_DRIVING, ApiRequest.LANGUAGE);
        call.enqueue(this);

        // draw hotel
//        getHandle().post(runAddMarkerMotel);
        if (!origin.equals("")) {
            CameraPosition cameraPosition = new CameraPosition(MapUtils.stringLatLngToLatLng(origin), 12, 12, 12);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate);
        }



        CameraUpdate cameraUpdate =
                CameraUpdateFactory.
                        newCameraPosition(new CameraPosition(MapUtils.stringLatLngToLatLng(mPlace.mLatLng),12,12,12));
        mMap.animateCamera(cameraUpdate);

    }


    @Override
    public void onResponse(Response<RoutesLocation> response, Retrofit retrofit) {
        if (response.body() != null) {

            try {
                String distance = response.body().routes.get(0).legs.get(0).distance.text;
                String duration = response.body().routes.get(0).legs.get(0).duration.text;
                mTxtDetailDirection.setText(distance + " : " + duration);

                RoutesLocation routesLocation = response.body();
                this.routesLocation = routesLocation;
                drawDirectionOnMap(routesLocation);

                mListDirectionDetailInfo.clear();
                mListDirectionDetailInfo.addAll(routesLocation.routes.get(0).legs.get(0).steps);

                navigateDirectionAdapter = new NavigateDirectionAdapter(mContext, this);
                navigateDirectionAdapter.setDatas(routesLocation.routes.get(0).legs.get(0).steps);

                if (rvNavigate !=null){
                    rvNavigate.setAdapter(navigateDirectionAdapter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Throwable t) {
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_map_marker))
                .position(MapUtils.stringLatLngToLatLng(mPlace.mLatLng));

        mMap.addMarker(markerOptions);
    }


    @Override
    public void onStop() {
        super.onStop();
        mGoogleMap.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mGoogleMap.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleMap.onDestroy();
    }


    private void settingUiMap(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(mContext).setCancelable(true)
                        .setMessage(R.string.msg_permission_map)
                        .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            } else {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
                mMap.setMyLocationEnabled(true);
                settingUiMap(mMap);
            }
        }
    }

    private boolean checkPermission() {
        boolean fineLocation = (mContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean coarseLocation = (mContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        return fineLocation && coarseLocation;
    }

    private void setDialogShowRequestPermission() {
        new AlertDialog.Builder(mContext).setCancelable(true)
                .setCancelable(false)
                .setMessage(R.string.msg_permission_fine_location)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void drawDirectionOnMap(final RoutesLocation routesLocation) {
        try {
            getHandle().post(new Runnable() {
                @Override
                public void run() {
                    if (routesLocation.routes.size() != 0 && routesLocation.routes.get(0).legs.size() != 0) {
                        AnalyzeSteps analyzeSteps = new AnalyzeSteps(routesLocation.routes.get(0).legs.get(0).steps);
                        ArrayList<LatLng> latLngs = analyzeSteps.getLatLngs();
                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#006DF0"));
                        polylineOptions.width(10);
                        polylineOptions.zIndex(30);
                        polylineOptions.geodesic(true);
                        polylineOptions.addAll(latLngs);
                        mMap.addPolyline(polylineOptions);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : latLngs) {
                            builder.include(latLng);
                        }
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 4);
                        mMap.animateCamera(cu);

                        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)).position(latLngs.get(latLngs.size() - 1));
                        mMap.addMarker(markerOptions);
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable runLoadMotelLocation = new Runnable() {
        @Override
        public void run() {
            String restedSelection = DbContracts.TableRested.RESTED_ID_PLACE;
            String[] args = new String[]{mPlace.mIdPlace};
            mResteds = TableRested.getInstance().getListData(restedSelection, args, null);
        }
    };

    private Runnable runLoadMap = new Runnable() {
        @Override
        public void run() {
            mGoogleMap.onCreate(null);
            mGoogleMap.onResume();
            mGoogleMap.getMapAsync(PlaceDetailMapFragment.this);
        }
    };


    // item on dialog direction navigate direction
    @Override
    public void onRecyclerViewItemClick(BaseEntity dataItem, View view, int position) {
        RoutesLocation.RoutesBean.LegsBean.StepsBean stepsBean = (RoutesLocation.RoutesBean.LegsBean.StepsBean) dataItem;

        CameraUpdate cameraUpdate =
                CameraUpdateFactory.
                        newCameraPosition(new CameraPosition(
                                new LatLng(stepsBean.startLocation.lat,
                                        stepsBean.startLocation.lat),17,17,17));
        mMap.animateCamera(cameraUpdate);
    }

}
