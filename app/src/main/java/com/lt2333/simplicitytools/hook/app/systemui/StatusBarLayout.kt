package com.lt2333.simplicitytools.hook.app.systemui

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cn.fkj233.ui.activity.dp2px
import com.lt2333.simplicitytools.util.XSPUtils
import com.lt2333.simplicitytools.util.findClass
import com.lt2333.simplicitytools.util.getObjectField
import com.lt2333.simplicitytools.util.hookAfterMethod
import com.lt2333.simplicitytools.util.xposed.base.HookRegister
import de.robv.android.xposed.XposedHelpers


@SuppressLint("StaticFieldLeak")
object StatusBarLayout : HookRegister() {

    private var mLeftLayout: LinearLayout? = null
    private var mRightLayout: LinearLayout? = null
    private var mCenterLayout: LinearLayout? = null
    private var statusBar: ViewGroup? = null

    private var statusBarLeft = 0
    private var statusBarTop = 0
    private var statusBarRight = 0
    private var statusBarBottom = 0

    override fun init() {
        when (XSPUtils.getInt("status_bar_layout_mode", 0)) {
            //默认
            0 -> return
            //时钟居中
            1 -> {
                val collapsedStatusBarFragmentClass =
                    "com.android.systemui.statusbar.phone.CollapsedStatusBarFragment".findClass(
                        getDefaultClassLoader()
                    )

                collapsedStatusBarFragmentClass.hookAfterMethod(
                    "onViewCreated",
                    View::class.java,
                    Bundle::class.java
                ) { param ->
                    val MiuiPhoneStatusBarView: ViewGroup =
                        param.thisObject.getObjectField("mStatusBar") as ViewGroup
                    val context: Context = MiuiPhoneStatusBarView.context
                    val res: Resources = MiuiPhoneStatusBarView.resources
                    val statusBarId: Int =
                        res.getIdentifier("status_bar", "id", "com.android.systemui")
                    val statusBarContentsId: Int =
                        res.getIdentifier("status_bar_contents", "id", "com.android.systemui")
                    val systemIconAreaId: Int =
                        res.getIdentifier("system_icon_area", "id", "com.android.systemui")
                    val clockId: Int = res.getIdentifier("clock", "id", "com.android.systemui")
                    val phoneStatusBarLeftContainerId: Int =
                        res.getIdentifier(
                            "phone_status_bar_left_container",
                            "id",
                            "com.android.systemui"
                        )
                    val notificationIconAreaInnerId: Int =
                        res.getIdentifier(
                            "notification_icon_area_inner",
                            "id",
                            "com.android.systemui"
                        )
                    statusBar = MiuiPhoneStatusBarView.findViewById(statusBarId)
                    val statusBarContents: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(statusBarContentsId)
                    if (statusBar == null) return@hookAfterMethod
                    val clock: TextView = MiuiPhoneStatusBarView.findViewById(clockId)
                    val phoneStatusBarLeftContainer: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(phoneStatusBarLeftContainerId)
                    val notificationIconAreaInner: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(notificationIconAreaInnerId)
                    val systemIconArea: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(systemIconAreaId)

                    (clock.parent as ViewGroup).removeView(clock)
                    (phoneStatusBarLeftContainer.parent as ViewGroup).removeView(
                        phoneStatusBarLeftContainer
                    )
                    (notificationIconAreaInner.parent as ViewGroup).removeView(
                        notificationIconAreaInner
                    )
                    (systemIconArea.parent as ViewGroup).removeView(systemIconArea)

                    val mConstraintLayout =
                        ConstraintLayout(context).also {
                            it.layoutParams = ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                    mConstraintLayout.addView(notificationIconAreaInner)

                    val fullscreen_notification_icon_area_lp = LinearLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    )

                    notificationIconAreaInner.layoutParams = fullscreen_notification_icon_area_lp

                    //增加一个左对齐布局
                    mLeftLayout = LinearLayout(context)
                    val LeftLp: LinearLayout.LayoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
                    mLeftLayout!!.layoutParams = LeftLp
                    mLeftLayout!!.gravity = Gravity.START or Gravity.CENTER_VERTICAL

                    //增加一个居中布局
                    mCenterLayout = LinearLayout(context)
                    val CenterLp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    mCenterLayout!!.layoutParams = CenterLp
                    mCenterLayout!!.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                    mRightLayout = LinearLayout(context)
                    val RightLp: LinearLayout.LayoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
                    mRightLayout!!.layoutParams = RightLp
                    mRightLayout!!.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    mLeftLayout!!.addView(phoneStatusBarLeftContainer)
                    mLeftLayout!!.addView(mConstraintLayout)

                    mCenterLayout!!.addView(clock)
                    mRightLayout!!.addView(systemIconArea)
                    statusBarContents.addView(mLeftLayout, 0)
                    statusBarContents.addView(mCenterLayout)
                    statusBarContents.addView(mRightLayout)

                    statusBarLeft = statusBar!!.paddingLeft
                    statusBarTop = statusBar!!.paddingTop
                    statusBarRight = statusBar!!.paddingRight
                    statusBarBottom = statusBar!!.paddingBottom


                    if (XSPUtils.getBoolean("layout_compatibility_mode", false)) {
                        val customLeftMargin = XSPUtils.getInt("status_bar_left_margin", 0)
                        if (customLeftMargin != 0) {
                            statusBarLeft = customLeftMargin
                        }

                        val customRightMargin = XSPUtils.getInt("status_bar_right_margin", 0)
                        if (customRightMargin != 0) {
                            statusBarRight = customRightMargin
                        }
                        updateLayout(context)
                    }
                }
                val phoneStatusBarViewClass =
                    "com.android.systemui.statusbar.phone.PhoneStatusBarView".findClass(
                        getDefaultClassLoader()
                    )

                phoneStatusBarViewClass.hookAfterMethod("updateLayoutForCutout") {
                    if (XSPUtils.getBoolean("layout_compatibility_mode", false)) {
                        val context = (it.thisObject as ViewGroup).context
                        updateLayout(context)
                    }
                }
            }
            //时钟居右
            2 -> {
                val collapsedStatusBarFragmentClass =
                    "com.android.systemui.statusbar.phone.CollapsedStatusBarFragment".findClass(
                        getDefaultClassLoader()
                    )

                collapsedStatusBarFragmentClass.hookAfterMethod(
                    "onViewCreated",
                    View::class.java,
                    Bundle::class.java
                ) { param ->
                    val MiuiPhoneStatusBarView: ViewGroup =
                        param.thisObject.getObjectField("mStatusBar") as ViewGroup
                    val context: Context = MiuiPhoneStatusBarView.context
                    val res: Resources = MiuiPhoneStatusBarView.resources

                    //组件ID
                    val statusBarId: Int =
                        res.getIdentifier("status_bar", "id", "com.android.systemui")
                    val clockId: Int = res.getIdentifier("clock", "id", "com.android.systemui")
                    val batteryId: Int = res.getIdentifier("battery", "id", "com.android.systemui")

                    //查找组件
                    statusBar = MiuiPhoneStatusBarView.findViewById(statusBarId)
                    if (statusBar == null) return@hookAfterMethod
                    val clock: TextView = MiuiPhoneStatusBarView.findViewById(clockId)
                    val battery: ViewGroup = MiuiPhoneStatusBarView.findViewById(batteryId)

                    //新建布局
                    val RightLp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).also {
                        it.marginStart = dp2px(context, 5f)
                    }
                    mRightLayout = LinearLayout(context).also {
                        it.layoutParams = RightLp
                    }

                    //添加布局与组件
                    battery.addView(mRightLayout)
                    (clock.parent as ViewGroup).removeView(clock)
                    mRightLayout!!.addView(clock)
                }
            }
            //时钟居中+图标居左
            3 -> {
                val collapsedStatusBarFragmentClass =
                    "com.android.systemui.statusbar.phone.CollapsedStatusBarFragment".findClass(
                        getDefaultClassLoader()
                    )

                collapsedStatusBarFragmentClass.hookAfterMethod(
                    "onViewCreated",
                    View::class.java,
                    Bundle::class.java
                ) { param ->
                    val MiuiPhoneStatusBarView: ViewGroup =
                        param.thisObject.getObjectField("mStatusBar") as ViewGroup
                    val context: Context = MiuiPhoneStatusBarView.context
                    val res: Resources = MiuiPhoneStatusBarView.resources
                    val statusBarId: Int =
                        res.getIdentifier("status_bar", "id", "com.android.systemui")
                    val statusBarContentsId: Int =
                        res.getIdentifier("status_bar_contents", "id", "com.android.systemui")
                    val systemIconAreaId: Int =
                        res.getIdentifier("system_icon_area", "id", "com.android.systemui")
                    val clockId: Int = res.getIdentifier("clock", "id", "com.android.systemui")
                    val phoneStatusBarLeftContainerId: Int =
                        res.getIdentifier(
                            "phone_status_bar_left_container",
                            "id",
                            "com.android.systemui"
                        )
                    val fullscreenNotificationIconAreaId: Int =
                        res.getIdentifier(
                            "fullscreen_notification_icon_area",
                            "id",
                            "com.android.systemui"
                        )
                    val statusIconsId: Int =
                        res.getIdentifier(
                            "statusIcons",
                            "id",
                            "com.android.systemui"
                        )
                    val systemIconsId: Int =
                        res.getIdentifier(
                            "system_icons",
                            "id",
                            "com.android.systemui"
                        )
                    val batteryId: Int =
                        res.getIdentifier(
                            "battery",
                            "id",
                            "com.android.systemui"
                        )
                    val notificationIconAreaInnerId: Int =
                        res.getIdentifier(
                            "notification_icon_area_inner",
                            "id",
                            "com.android.systemui"
                        )

                    statusBar = MiuiPhoneStatusBarView.findViewById(statusBarId)
                    val statusBarContents: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(statusBarContentsId)
                    if (statusBar == null) return@hookAfterMethod
                    val clock: TextView = MiuiPhoneStatusBarView.findViewById(clockId)
                    val phoneStatusBarLeftContainer: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(phoneStatusBarLeftContainerId)
                    val fullscreenNotificationIconArea: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(fullscreenNotificationIconAreaId)
                    val systemIconArea: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(systemIconAreaId)
                    val statusIcons: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(statusIconsId)
                    val systemIcons: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(systemIconsId)
                    val battery: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(batteryId)
                    val notificationIconAreaInner: ViewGroup =
                        MiuiPhoneStatusBarView.findViewById(notificationIconAreaInnerId)

                    (clock.parent as ViewGroup).removeView(clock)
                    (phoneStatusBarLeftContainer.parent as ViewGroup).removeView(
                        phoneStatusBarLeftContainer
                    )
                    (systemIconArea.parent as ViewGroup).removeView(systemIconArea)
                    (statusIcons.parent as ViewGroup).removeView(statusIcons)
                    (systemIcons.parent as ViewGroup).removeView(systemIcons)
                    (battery.parent as ViewGroup).removeView(battery)
                    (fullscreenNotificationIconArea.parent as ViewGroup).removeView(
                        fullscreenNotificationIconArea
                    )

                    val mConstraintLayout =
                        ConstraintLayout(context).also {
                            it.layoutParams = ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                    mConstraintLayout.addView(fullscreenNotificationIconArea)
                    mConstraintLayout.addView(battery)


                    battery.layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    ).also {
                        it.endToEnd = 0
                    }


                    fullscreenNotificationIconArea.layoutParams = ConstraintLayout.LayoutParams(
                        0,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    ).also {
                        it.startToEnd = batteryId
                        it.endToEnd = 0
                    }
                    fullscreenNotificationIconArea.layoutDirection = View.LAYOUT_DIRECTION_RTL


                    //增加一个左对齐布局
                    mLeftLayout = LinearLayout(context)
                    val LeftLp: LinearLayout.LayoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
                    mLeftLayout!!.layoutParams = LeftLp
                    mLeftLayout!!.gravity = Gravity.START or Gravity.CENTER_VERTICAL

                    //增加一个居中布局
                    mCenterLayout = LinearLayout(context)
                    val CenterLp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    mCenterLayout!!.layoutParams = CenterLp
                    mCenterLayout!!.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL

                    //增加一个右布局
                    mRightLayout = LinearLayout(context)
                    val RightLp: LinearLayout.LayoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
                    mRightLayout!!.layoutParams = RightLp
                    mRightLayout!!.gravity = Gravity.END or Gravity.CENTER_VERTICAL


                    mLeftLayout!!.addView(phoneStatusBarLeftContainer)
                    mLeftLayout!!.addView(statusIcons)
                    statusIcons.layoutDirection = View.LAYOUT_DIRECTION_RTL

                    mCenterLayout!!.addView(clock)

                    mRightLayout!!.addView(mConstraintLayout)
                    fullscreenNotificationIconArea.layoutDirection = View.LAYOUT_DIRECTION_RTL


                    statusBarContents.addView(mLeftLayout, 0)
                    statusBarContents.addView(mCenterLayout)
                    statusBarContents.addView(mRightLayout)



                    statusBarLeft = statusBar!!.paddingLeft
                    statusBarTop = statusBar!!.paddingTop
                    statusBarRight = statusBar!!.paddingRight
                    statusBarBottom = statusBar!!.paddingBottom


                    if (XSPUtils.getBoolean("layout_compatibility_mode", false)) {
                        val customLeftMargin = XSPUtils.getInt("status_bar_left_margin", 0)
                        if (customLeftMargin != 0) {
                            statusBarLeft = customLeftMargin
                        }

                        val customRightMargin = XSPUtils.getInt("status_bar_right_margin", 0)
                        if (customRightMargin != 0) {
                            statusBarRight = customRightMargin
                        }
                        updateLayout(context)
                    }
                }
                //兼容模式
                val phoneStatusBarViewClass =
                    "com.android.systemui.statusbar.phone.PhoneStatusBarView".findClass(
                        getDefaultClassLoader()
                    )
                phoneStatusBarViewClass.hookAfterMethod("updateLayoutForCutout") {
                    if (XSPUtils.getBoolean("layout_compatibility_mode", false)) {
                        val context = (it.thisObject as ViewGroup).context
                        updateLayout(context)
                    }
                }
                //解决重叠
                val miuiCollapsedStatusBarFragmentClass =
                    "com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment".findClass(
                        getDefaultClassLoader()
                    )

                miuiCollapsedStatusBarFragmentClass.hookAfterMethod(
                    "showClock",
                    Boolean::class.java
                ) {
                    val MiuiPhoneStatusBarView =
                        XposedHelpers.getObjectField(it.thisObject, "mStatusBar") as ViewGroup
                    val res = MiuiPhoneStatusBarView.resources
                    val status_bar_ID =
                        res.getIdentifier("status_bar", "id", "com.android.systemui")
                    val status_bar = MiuiPhoneStatusBarView.findViewById<ViewGroup>(status_bar_ID)
                    //非锁屏下整个状态栏布局
                    val keyguardMgr =
                        status_bar.context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    if (keyguardMgr.isKeyguardLocked) {
                        status_bar!!.visibility = View.GONE
                    } else {
                        status_bar!!.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateLayout(context: Context) {
        //判断屏幕方向
        val mConfiguration: Configuration = context.resources.configuration
        if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLeftLayout!!.setPadding(statusBarLeft, 0, 0, 0)
            mRightLayout!!.setPadding(0, 0, statusBarRight, 0)
            statusBar!!.setPadding(0, statusBarTop, 0, statusBarBottom)
        } else {
            //横屏状态
            mLeftLayout!!.setPadding(175, 0, 0, 0)
            mRightLayout!!.setPadding(0, 0, 175, 0)
            statusBar!!.setPadding(0, statusBarTop, 0, statusBarBottom)
        }
    }

}