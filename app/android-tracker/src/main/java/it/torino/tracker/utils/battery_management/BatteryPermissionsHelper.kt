/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

/**
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */
package it.torino.tracker.utils.battery_management

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build

class BatteryPermissionsHelper {
    /***
     * Samsung
     */
    private val BRAND_SAMSUNG = "samsung"
    private val PACKAGE_SAMSUNG_MAIN = "com.samsung.android.lool"
    private val PACKAGE_SAMSUNG_COMPONENT = "com.samsung.android.sm.ui.battery.BatteryActivity"

    /***
     * Xiaomi
     */
    private val BRAND_XIAOMI = "xiaomi"
    private val PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter"
    private val PACKAGE_XIAOMI_COMPONENT = "com.miui.powercenter.PowerSettings"

    /***
     * ASUS ROG
     */
    private val BRAND_ASUS = "asus"
    private val PACKAGE_ASUS_MAIN = "com.asus.mobilemanager"
    private val PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings"

    /***
     * Honor
     */
    private val BRAND_HUAWEI = "huawei"
    private val PACKAGE_HUAWEI_MAIN = "com.huawei.systemmanager"


    private val BRAND_HONOR = "honor"
    private val PACKAGE_HONOR_MAIN = "com.huawei.systemmanager"
    private val PACKAGE_HONOR_COMPONENT =
        "com.huawei.systemmanager"


    fun getBatteryPermission(context: Context) {
        when (Build.BRAND.lowercase()) {
            BRAND_ASUS -> batteryPermissionsAsus(context)
            BRAND_XIAOMI -> batteryPermissionsXiaomi(context)
            BRAND_HONOR -> batteryPermissionsHonor(context)
            BRAND_HUAWEI -> batteryPermissionsHuawei(context)
            BRAND_SAMSUNG -> batteryPermissionsSamsung(context)
        }
    }

    private fun batteryPermissionsAsus(context: Context) {
        if (isPackageExists(context, PACKAGE_ASUS_MAIN)) {
            try {
                startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showAlert(context: Context, onClickListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context).setTitle("Allow AutoStart")
            .setMessage("Please enable auto start in settings.")
            .setPositiveButton("Allow", onClickListener).show().setCancelable(false)
    }

    private fun batteryPermissionsXiaomi(context: Context) {
        if (isPackageExists(context, PACKAGE_XIAOMI_MAIN)) {
            try {
                startIntent(context, PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun batteryPermissionsHonor(context: Context) {
        if (isPackageExists(context, PACKAGE_HONOR_MAIN)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun batteryPermissionsHuawei(context: Context) {
        if (isPackageExists(context, PACKAGE_HUAWEI_MAIN)) {
            showAlert(context) { dialog, which ->
                try {
                    context.packageManager.getLaunchIntentForPackage(PACKAGE_HUAWEI_MAIN)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun batteryPermissionsSamsung(context: Context) {
        if (isPackageExists(context, PACKAGE_SAMSUNG_MAIN)) {
            showAlert(context) { dialog, which ->
                try {
                    startIntent(context, PACKAGE_SAMSUNG_MAIN, PACKAGE_SAMSUNG_COMPONENT)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    @Throws(Exception::class)
    private fun startIntent(context: Context, packageName: String, componentName: String) {
        try {
            val intent = Intent()
            intent.component = ComponentName(packageName, componentName)
            context.startActivity(intent)
        } catch (var5: Exception) {
            var5.printStackTrace()
            throw var5
        }
    }

    private fun isPackageExists(context: Context, targetPackage: String): Boolean {
        val packages: List<ApplicationInfo>
        val pm = context.packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == targetPackage) {
                return true
            }
        }
        return false
    }

}