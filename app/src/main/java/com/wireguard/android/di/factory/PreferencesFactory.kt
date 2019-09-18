/*
 * Copyright © 2017-2019 WireGuard LLC.
 * Copyright © 2018-2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.di.factory

import android.app.Application
import com.tencent.mmkv.MMKV
import com.wireguard.android.util.ApplicationPreferences

object PreferencesFactory {
    fun init(context: Application): ApplicationPreferences {
        MMKV.initialize(context)
        return ApplicationPreferences(context)
    }
}
