package com.mobile.guava.android.log;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.mobile.guava.jvm.date.DateUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DeviceData {
    public final static String TAG = "DeviceData";
    public final static String KEY_INSTALL_ID = "installId";
    private static final String[] ROOT_INDICATORS = new String[]{
            // Common binaries
            "/system/xbin/su",
            "/system/bin/su",
            // < Android 5.0
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            // >= Android 5.0
            "/system/app/Superuser",
            "/system/app/SuperSU",
            // Fallback
            "/system/xbin/daemonsu",
            // Systemless root
            "/su/bin"
    };

    private final boolean emulator;
    private final Context appContext;
    private final SharedPreferences sharedPref;
    private final Resources resources;
    private final DisplayMetrics displayMetrics;
    private final String id;

    @Nullable
    Float screenDensity;

    @Nullable
    Integer dpi;

    @Nullable
    String screenResolution;

    @NonNull
    String locale;

    @NonNull
    String[] cpuAbi;

    public DeviceData(@NonNull Context appContext, @NonNull SharedPreferences sharedPref) {
        this.appContext = appContext;
        this.sharedPref = sharedPref;
        resources = appContext.getResources();

        if (resources != null) {
            displayMetrics = resources.getDisplayMetrics();
        } else {
            displayMetrics = null;
        }

        screenDensity = getScreenDensity();
        dpi = getScreenDensityDpi();
        screenResolution = getScreenResolution();
        locale = getLocale();
        cpuAbi = getCpuAbi();
        emulator = PhoneUtilsKt.isEmulator(appContext);
        id = retrieveUniqueInstallId();
    }

    public Map<String, Object> getDeviceDataSummary() {
        Map<String, Object> map = new HashMap<>();
        map.put("manufacturer", Build.MANUFACTURER);
        map.put("model", Build.MODEL);
        map.put("jailbroken", isRooted());
        map.put("osName", "android");
        map.put("osVersion", Build.VERSION.RELEASE);
        return map;
    }

    public Map<String, Object> getDeviceData() {
        Map<String, Object> map = getDeviceDataSummary();
        map.put("id", id);
        map.put("freeMemory", calculateFreeMemory());
        map.put("totalMemory", calculateTotalMemory());
        map.put("freeDisk", calculateFreeDisk());
        map.put("orientation", calculateOrientation());
        return map;
    }

    public Map<String, Object> getDeviceMetaData() {
        Map<String, Object> map = new HashMap<>();
        map.put("batteryLevel", getBatteryLevel());
        map.put("charging", isCharging());
        map.put("locationStatus", getLocationStatus());
        map.put("networkAccess", getNetworkAccess());
        map.put("time", getTime());
        map.put("brand", Build.BRAND);
        map.put("apiLevel", Build.VERSION.SDK_INT);
        map.put("osBuild", Build.DISPLAY);
        map.put("locale", locale);
        map.put("screenDensity", screenDensity);
        map.put("dpi", dpi);
        map.put("emulator", emulator);
        map.put("screenResolution", screenResolution);
        map.put("cpuAbi", cpuAbi);
        return map;
    }

    /**
     * Check if the current Android device is rooted
     */
    private boolean isRooted() {
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true;
        }
        try {
            for (String candidate : ROOT_INDICATORS) {
                if (new File(candidate).exists()) {
                    return true;
                }
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    /**
     * The screen density scaling factor of the current Android device
     */
    @Nullable
    private Float getScreenDensity() {
        if (displayMetrics != null) {
            return displayMetrics.density;
        } else {
            return null;
        }
    }

    /**
     * The screen density of the current Android device in dpi, eg. 320
     */
    @Nullable
    private Integer getScreenDensityDpi() {
        if (displayMetrics != null) {
            return displayMetrics.densityDpi;
        } else {
            return null;
        }
    }

    /**
     * The screen resolution of the current Android device in px, eg. 1920x1080
     */
    @Nullable
    private String getScreenResolution() {
        if (displayMetrics != null) {
            int max = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            int min = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            return String.format(Locale.US, "%dx%d", max, min);
        } else {
            return null;
        }
    }

    /**
     * Get the locale of the current Android device, eg en_US
     */
    @NonNull
    private String getLocale() {
        return Locale.getDefault().toString();
    }

    /**
     * Get the unique id for the current app installation, creating a unique UUID if needed
     */
    @Nullable
    private String retrieveUniqueInstallId() {
        String installId = sharedPref.getString(KEY_INSTALL_ID, null);
        if (installId == null) {
            installId = UUID.randomUUID().toString();
            sharedPref.edit().putString(KEY_INSTALL_ID, installId).apply();
        }
        return installId;
    }

    /**
     * Gets information about the CPU / API
     */
    @NonNull
    private String[] getCpuAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return SupportedAbiWrapper.getSupportedAbis();
        }
        return Abi2Wrapper.getAbi1andAbi2();
    }

    /**
     * Get the free disk space on the smallest disk
     */
    @Nullable
    @SuppressWarnings("deprecation") // ignore blockSizeLong suggestions for now (requires API 18)
    private Long calculateFreeDisk() {
        try {
            StatFs externalStat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long externalBytesAvailable =
                    (long) externalStat.getBlockSize() * (long) externalStat.getBlockCount();

            StatFs internalStat = new StatFs(Environment.getDataDirectory().getPath());
            long internalBytesAvailable =
                    (long) internalStat.getBlockSize() * (long) internalStat.getBlockCount();

            return Math.min(internalBytesAvailable, externalBytesAvailable);
        } catch (Exception exception) {
            Log.w(TAG, "Could not get freeDisk");
        }
        return null;
    }

    /**
     * Get the amount of memory remaining that the VM can allocate
     */
    private long calculateFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        if (runtime.maxMemory() != Long.MAX_VALUE) {
            return runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();
        } else {
            return runtime.freeMemory();
        }
    }

    /**
     * Get the device orientation, eg. "landscape"
     */
    @Nullable
    private String calculateOrientation() {
        String orientation = null;

        if (resources != null) {
            switch (resources.getConfiguration().orientation) {
                case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                    orientation = "landscape";
                    break;
                case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                    orientation = "portrait";
                    break;
                default:
                    break;
            }
        }
        return orientation;
    }

    /**
     * Get the current battery charge level, eg 0.3
     */
    @Nullable
    private Float getBatteryLevel() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, ifilter);

            return batteryStatus.getIntExtra("level", -1)
                    / (float) batteryStatus.getIntExtra("scale_home_action_item", -1);
        } catch (Exception exception) {
            Log.w(TAG, "Could not get batteryLevel");
        }
        return null;
    }

    /**
     * Is the device currently charging/full battery?
     */
    @Nullable
    private Boolean isCharging() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra("status", -1);
            return (status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL);
        } catch (Exception exception) {
            Log.w(TAG, "Could not get charging status");
        }
        return null;
    }

    /**
     * Get the current status of location services
     */
    @Nullable
    @SuppressWarnings("deprecation") // LOCATION_PROVIDERS_ALLOWED is deprecated
    private String getLocationStatus() {
        try {
            ContentResolver cr = appContext.getContentResolver();
            String providersAllowed =
                    Settings.Secure.getString(cr, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (providersAllowed != null && providersAllowed.length() > 0) {
                return "allowed";
            } else {
                return "disallowed";
            }
        } catch (Exception exception) {
            Log.w(TAG, "Could not get locationStatus");
        }
        return null;
    }

    /**
     * Get the current status of network access, eg "cellular"
     */
    @Nullable
    private String getNetworkAccess() {
        try {
            ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE
            );
            if (cm == null) {
                return null;
            }

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                if (activeNetwork.getType() == 1) {
                    return "wifi";
                } else if (activeNetwork.getType() == 9) {
                    return "ethernet";
                } else {
                    // We default to cellular as the other enums are all cellular in some
                    // form or another
                    return "cellular";
                }
            } else {
                return "none";
            }
        } catch (Exception exception) {
            Log.w(TAG, "Could not get network access information, we "
                    + "recommend granting the 'android.permission.ACCESS_NETWORK_STATE' permission");
        }
        return null;
    }

    /**
     * Get the current time on the device, in ISO8601 format.
     */
    @NonNull
    private String getTime() {
        return DateUtils.toIso8601(new Date());
    }

    /**
     * Wrapper class to allow the test framework to use the correct version of the CPU / ABI
     */
    static class SupportedAbiWrapper {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        static String[] getSupportedAbis() {
            return Build.SUPPORTED_ABIS;
        }
    }

    /**
     * Wrapper class to allow the test framework to use the correct version of the CPU / ABI
     */
    static class Abi2Wrapper {
        @NonNull
        @SuppressWarnings("deprecation") // new API already used elsewhere
        static String[] getAbi1andAbi2() {
            return new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
    }

    /**
     * Get the total memory available on the current Android device, in bytes
     */
    public static long calculateTotalMemory() {
        Runtime runtime = Runtime.getRuntime();
        if (runtime.maxMemory() != Long.MAX_VALUE) {
            return runtime.maxMemory();
        } else {
            return runtime.totalMemory();
        }
    }
}
