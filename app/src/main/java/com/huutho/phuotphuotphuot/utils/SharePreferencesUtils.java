package com.huutho.phuotphuotphuot.utils;

import com.huutho.phuotphuotphuot.app.Config;
import com.huutho.phuotphuotphuot.base.preferences.BasePreferences;

/**
 * Created by HuuTho on 1/18/2017.
 */
public class SharePreferencesUtils extends BasePreferences {

    private static final String KEY_FIRST_RUN_APP = "key.first.run.app";
    private static final String KEY_LAST_KNOW_LOCATION = "key.last.know.location";
    private static final String KEY_LANGUAGE = "key.language";

    public static SharePreferencesUtils getInstances() {
        return new SharePreferencesUtils();
    }

    private SharePreferencesUtils(){
        super();
    }

    @Override
    public String getNamePreferences() {
        return Config.SHARE_PRERERENCES_NAME;
    }

    public void setFirstRunApp(boolean isFirst) {
        getEditor().putBoolean(KEY_FIRST_RUN_APP, isFirst).commit();
    }

    public boolean getFirstRunApp() {
      return getSharePreferences().getBoolean(KEY_FIRST_RUN_APP,true);
    }
    public void setLastKnowLocation(String location){
        getEditor().putString(KEY_LAST_KNOW_LOCATION,location).commit();
    }
    public String getLastKnowLocation(){
        return getSharePreferences().getString(KEY_LAST_KNOW_LOCATION,"");
    }

    public void setLanguage(String lang){
        getEditor().putString(KEY_LANGUAGE,lang).commit();
    }

    public String getLanguage(){
        return getSharePreferences().getString(KEY_LANGUAGE,"vi");
    }
}
