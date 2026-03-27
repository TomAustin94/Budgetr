package com.budgetr.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.budgetr.app.data.local.entity.AccountBalanceEntity
import com.budgetr.app.data.model.TransactionCategory
import dagger.hilt.android.EntryPointAccessors
import java.text.NumberFormat
import java.util.Locale

class BudgetrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val balances = try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            ).accountBalanceDao().getAllSync()
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            WidgetContent(context = context, balances = balances)
        }
    }
}

@Composable
private fun WidgetContent(context: Context, balances: List<AccountBalanceEntity>) {
    val bgColor = Color(0xFF1B5E20)
    val positiveColor = Color(0xFF69F0AE)
    val negativeColor = Color(0xFFFF5252)
    val subtleWhite = Color(0xAAFFFFFF)
    val dividerColor = Color(0x33FFFFFF)

    val total = balances.sumOf { it.remainingBalance }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(10.dp)
    ) {
        // Header: title + total
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "Budgetr",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = formatCurrency(total),
                style = TextStyle(
                    color = ColorProvider(if (total >= 0) positiveColor else negativeColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(GlanceModifier.height(6.dp))

        // Per-account balances
        if (balances.isEmpty()) {
            Text(
                text = "No data — open app to sync",
                style = TextStyle(color = ColorProvider(subtleWhite), fontSize = 10.sp)
            )
        } else {
            balances.forEach { account ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = account.account,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = ColorProvider(subtleWhite),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = formatCurrency(account.remainingBalance),
                        style = TextStyle(
                            color = ColorProvider(
                                if (account.remainingBalance >= 0) positiveColor else negativeColor
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(GlanceModifier.height(6.dp))

        // Divider
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(dividerColor)
        ) {}

        Spacer(GlanceModifier.height(6.dp))

        // Quick-add buttons
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            QuickAddButton(context, "One Off", TransactionCategory.ONE_OFF_COST, Color(0xFFFF5252), modifier = GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(5.dp))
            QuickAddButton(context, "Fixed", TransactionCategory.FIXED_COST, Color(0xFFFFAB40), modifier = GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(5.dp))
            // Transfer opens the full-app form (needs destination account picker)
            QuickAddButton(context, "Transfer", TransactionCategory.TRANSFER, Color(0xFF90CAF9), fullApp = true, modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun QuickAddButton(
    context: Context,
    label: String,
    category: TransactionCategory,
    color: Color,
    fullApp: Boolean = false,
    modifier: GlanceModifier = GlanceModifier
) {
    val intent = Intent(context, QuickAddActivity::class.java).apply {
        putExtra(QuickAddActivity.EXTRA_CATEGORY, category.name)
        putExtra(QuickAddActivity.EXTRA_FULL_APP, fullApp)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Box(
        modifier = modifier
            .height(28.dp)
            .background(Color(0x33FFFFFF))
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun formatCurrency(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.UK).format(amount)
