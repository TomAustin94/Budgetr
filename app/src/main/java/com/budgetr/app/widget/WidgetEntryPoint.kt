package com.budgetr.app.widget

import com.budgetr.app.data.local.dao.AccountBalanceDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun accountBalanceDao(): AccountBalanceDao
}
