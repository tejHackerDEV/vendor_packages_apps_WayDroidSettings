package com.waydroid.settings.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.waydroid.settings.R
import java.lang.reflect.InvocationTargetException

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var switchEnablePCMode: SwitchPreferenceCompat
    private lateinit var switchEnabledWayDroidSystemUI: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        switchEnablePCMode = findPreference<Preference>(
            getString(R.string.key_switch_enable_pc_mode)
        ) as SwitchPreferenceCompat
        switchEnablePCMode.isChecked = getBooleanSystemProperties(PROPERTY_PC_MODE_KEY)
        switchEnabledWayDroidSystemUI = findPreference<Preference>(
            getString(R.string.key_switch_enable_bd_nav_bar)
        ) as SwitchPreferenceCompat
        switchEnabledWayDroidSystemUI.isChecked =
            getBooleanSystemProperties(PROPERTY_BD_SYSTEMUI_KEY)
        switchEnablePCMode.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference ->
                enablePCMode((preference as SwitchPreferenceCompat).isChecked)
                true
            }
        switchEnabledWayDroidSystemUI.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference ->
                enableWayDroidSystemUI((preference as SwitchPreferenceCompat).isChecked)
                true
            }
    }

    private fun enablePCMode(enable: Boolean) {
        Log.d(TAG, "enable pc mode $enable")
        setBooleanSystemProperties(PROPERTY_PC_MODE_KEY, enable)
    }

    private fun enableWayDroidSystemUI(enable: Boolean) {
        Log.d(TAG, "enable WayDroid SystemUI $enable")
        setBooleanSystemProperties(PROPERTY_BD_SYSTEMUI_KEY, enable)
        val context: Context? = activity
        if (context != null) {
            val packageName = context.packageName
            val intent = Intent("com.android.systemui.action.RESTART")
                .setData(Uri.parse("package://$packageName"))
            val cn = ComponentName(
                "com.android.systemui",
                "com.android.systemui.SysuiRestartReceiver"
            )
            intent.component = cn
            context.sendBroadcast(intent)
        }
    }

    private fun setBooleanSystemProperties(key: String, value: Boolean) {
        try {
            @SuppressLint("PrivateApi") val clazz = Class.forName(SYSTEM_PROPERTIES_CLASS_NAME)
            val setMethod = clazz.getMethod("set", String::class.java, String::class.java)
            setMethod.invoke(null, key, if (value) "true" else "false")
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "Failed to set value $value for $key")
        } catch (e: NoSuchMethodException) {
            Log.d(TAG, "Failed to set value $value for $key")
        } catch (e: IllegalAccessException) {
            Log.d(TAG, "Failed to set value $value for $key")
        } catch (e: InvocationTargetException) {
            Log.d(TAG, "Failed to set value $value for $key")
        }
    }

    private fun getBooleanSystemProperties(key: String): Boolean {
        try {
            @SuppressLint("PrivateApi")
            val clazz = Class.forName(SYSTEM_PROPERTIES_CLASS_NAME)
            val setMethod = clazz.getMethod(
                "getBoolean",
                String::class.java, Boolean::class.javaPrimitiveType
            )
            return setMethod.invoke(null, key, true) as Boolean
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "Failed to get value for $key")
        } catch (e: NoSuchMethodException) {
            Log.d(TAG, "Failed to get value for $key")
        } catch (e: IllegalAccessException) {
            Log.d(TAG, "Failed to get value for $key")
        } catch (e: InvocationTargetException) {
            Log.d(TAG, "Failed to get value for $key")
        }
        return true
    }

    companion object {
        private const val TAG = "BDSettingsFragment"
        private const val SYSTEM_PROPERTIES_CLASS_NAME = "android.os.SystemProperties"
        private const val PROPERTY_PC_MODE_KEY = "persist.sys.pcmode.enabled"
        private const val PROPERTY_BD_SYSTEMUI_KEY = "persist.sys.systemuiplugin.enabled"
    }
}
