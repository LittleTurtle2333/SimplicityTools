package com.lt2333.simplicitytools.hook.app

import com.lt2333.simplicitytools.hook.app.thememanager.RemoveAds
import com.lt2333.simplicitytools.util.xposed.base.AppRegister
import de.robv.android.xposed.callbacks.XC_LoadPackage

object ThemeManager: AppRegister() {
    override val packageName: String = "com.android.thememanager"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        autoInitHooks(lpparam,
            RemoveAds, //移除主题壁纸的广告
        )
    }
}