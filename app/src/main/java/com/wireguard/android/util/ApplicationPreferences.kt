/*
 * Copyright © 2017-2019 WireGuard LLC.
 * Copyright © 2018-2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.tencent.mmkv.MMKV
import kotlin.reflect.KProperty

class ApplicationPreferences(val context: Context) {

    private val onChangeMap: MutableMap<String, () -> Unit> = HashMap()
    private val onChangeListeners: MutableMap<String, MutableSet<OnPreferenceChangeListener>> = HashMap()
    private var onChangeCallback: ApplicationPreferencesChangeCallback? = null
    val mmkv: MMKV = MMKV.defaultMMKV()
    private val doNothing = { }
    private val restart = { restart() }
    private val restartActiveTunnels = { restartActiveTunnels() }

    init {
        if (!mmkv.getBoolean("import_success", false)) {
            mmkv.importFromSharedPreferences(PreferenceManager.getDefaultSharedPreferences(context))
            mmkv.putBoolean("import_success", true)
        }
    }

    var exclusions by StringPref("global_exclusions", "", restartActiveTunnels)
    val exclusionsArray: ArrayList<String>
        get() = exclusions.toArrayList()
    var useDarkTheme by BooleanPref("dark_theme", false)
    val forceUserspaceBackend by BooleanPref("force_userspace_backend", false, restart)
    val whitelistApps by BooleanPref("whitelist_exclusions", false, restartActiveTunnels)
    val allowTaskerIntegration by BooleanPref("allow_tasker_integration", false)
    val taskerIntegrationSecret by StringPref("intent_integration_secret", "")
    var lastUsedTunnel by StringPref("last_used_tunnel", "")
    val restoreOnBoot by BooleanPref("restore_on_boot", false)
    var runningTunnels by StringSetPref("enabled_configs", emptySet())
    var fingerprintAuth by BooleanPref("fingerprint_auth", false)

    fun registerCallback(callback: ApplicationPreferencesChangeCallback) {
        onChangeCallback = callback
    }

    fun unregisterCallback() {
        onChangeCallback = null
    }

    private fun restart() {
        onChangeCallback?.restart()
    }

    private fun restartActiveTunnels() {
        onChangeCallback?.restartActiveTunnels()
    }

    interface OnPreferenceChangeListener {
        fun onValueChanged(key: String, prefs: ApplicationPreferences, force: Boolean)
    }

    open inner class StringSetPref(key: String, defaultValue: Set<String>, onChange: () -> Unit = doNothing) :
            PrefDelegate<Set<String>>(key, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = mmkv.getStringSet(getKey(), defaultValue)
                ?: defaultValue

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(getKey(), value) }
        }
    }

    open inner class StringPref(key: String, defaultValue: String = "", onChange: () -> Unit = doNothing) :
            PrefDelegate<String>(key, defaultValue, onChange) {
        override fun onGetValue(): String = mmkv.getString(getKey(), defaultValue)
                ?: defaultValue

        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class BooleanPref(key: String, defaultValue: Boolean = false, onChange: () -> Unit = doNothing) :
            PrefDelegate<Boolean>(key, defaultValue, onChange) {
        override fun onGetValue(): Boolean = mmkv.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    abstract inner class PrefDelegate<T : Any>(val key: String, val defaultValue: T, private val onChange: () -> Unit) {

        private var cached = false
        protected var value: T = defaultValue

        init {
            onChangeMap[key] = { onValueChanged() }
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!cached) {
                value = onGetValue()
                cached = true
            }
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            discardCachedValue()
            onSetValue(value)
        }

        abstract fun onGetValue(): T

        abstract fun onSetValue(value: T)

        protected inline fun edit(body: MMKV.() -> Unit) {
            body(mmkv)
        }

        internal fun getKey() = key

        private fun onValueChanged() {
            discardCachedValue()
            onChange.invoke()
        }

        private fun discardCachedValue() {
            if (cached) {
                cached = false
                value.let(::disposeOldValue)
            }
        }

        open fun disposeOldValue(oldValue: T) {
        }
    }
}
