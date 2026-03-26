package com.budgetr.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import com.budgetr.app.data.model.TransactionCategory
import dagger.hilt.android.EntryPointAccessors
import java.text.NumberFormat
import java.util.Locale

class BudgetrWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalAvailable = try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            entryPoint.accountBalanceDao().getAllSync().sumOf { it.remainingBalance }
        } catch (e: Exception) {
            null
        }

        provideContent {
            WidgetContent(context = context, totalAvailable = totalAvailable)
        }
    }
}

@Composable
private fun WidgetContent(context: Context, totalAvailable: Double?) {
    val bgColor = Color(0xFF1B5E20)
    val positiveColor = Color(0xFF69F0AE)
    val negativeColor = Color(0xFFFF5252)
    val subtleWhite = Color(0xAAFFFFFF)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
    ) {
        // Title
        Text(
            text = "Budgetr",
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(GlanceModifier.height(4.dp))

        // Balance
        val balanceText = totalAvailable?.let { formatCurrency(it) } ?: "—"
        val balanceColor = when {
            totalAvailable == null -> Color.White
            totalAvailable >= 0 -> positiveColor
            else -> negativeColor
        }
        Text(
            text = balanceText,
            style = TextStyle(
                color = ColorProvider(balanceColor),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Total Available",
            style = TextStyle(
                color = ColorProvider(subtleWhite),
                fontSize = 10.sp
            )
        )

        Spacer(GlanceModifier.height(10.dp))

        // Quick-add row
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            QuickAddButton(
                context = context,
                label = "One Off",
                category = TransactionCategory.ONE_OFF_COST,
                color = Color(0xFFFF5252)
            )
            Spacer(GlanceModifier.width(6.dp))
            QuickAddButton(
                context = context,
                label = "Fixed",
                category = TransactionCategory.FIXED_COST,
                color = Color(0xFFFFAB40)
            )
            Spacer(GlanceModifier.width(6.dp))
            QuickAddButton(
                context = context,
                label = "Income",
                category = TransactionCategory.INCOME,
                color = positiveColor
            )
        }
    }
}

@Composable
private fun QuickAddButton(
    context: Context,
    label: String,
    category: TransactionCategory,
    color: Color
) {
    val intent = Intent(context, QuickAddActivity::class.java).apply {
        putExtra(QuickAddActivity.EXTRA_CATEGORY, category.name)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    Box(
        modifier = GlanceModifier
            .height(30.dp)
            .defaultWeight()
            .background(Color(0x33FFFFFF)) // 20% white overlay on green
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

private fun formatCurrency(amount: Double): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.UK)
    return fmt.format(amount)
}
