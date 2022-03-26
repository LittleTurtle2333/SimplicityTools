package com.lt2333.simplicitytools.hook.app.systemui

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.lt2333.simplicitytools.util.getObjectField
import com.lt2333.simplicitytools.util.hasEnable
import com.lt2333.simplicitytools.util.hookAfterMethod
import com.lt2333.simplicitytools.util.xposed.base.HookRegister

object HideBatteryIcon : HookRegister() {

    override fun init() {
        "com.android.systemui.statusbar.views.MiuiBatteryMeterView".hookAfterMethod(getDefaultClassLoader(), "updateResources") {

            //隐藏电池图标
            hasEnable("hide_battery_icon") {
                (it.thisObject.getObjectField("mBatteryIconView") as ImageView).visibility = View.GONE
                if (it.thisObject.getObjectField("mBatteryStyle") == 1) {
                    (it.thisObject.getObjectField("mBatteryDigitalView") as FrameLayout).visibility = View.GONE
                }
            }

            //隐藏电池内的百分比
            hasEnable("hide_battery_percentage_icon") {
                (it.thisObject.getObjectField("mBatteryPercentMarkView") as TextView).textSize = 0F
            }

            //隐藏电池百分号
            hasEnable("hide_battery_percentage_icon") {
                (it.thisObject.getObjectField("mBatteryPercentMarkView") as TextView).textSize = 0F
            }

            "com.android.systemui.statusbar.views.MiuiBatteryMeterView".hookAfterMethod(getDefaultClassLoader(), "updateChargeAndText") {
                //隐藏电池充电图标
                hasEnable("hide_battery_charging_icon") {
                    (it.thisObject.getObjectField("mBatteryChargingInView") as ImageView).visibility = View.GONE
                    (it.thisObject.getObjectField("mBatteryChargingView") as ImageView).visibility = View.GONE
                }
            }
        }
    }

}