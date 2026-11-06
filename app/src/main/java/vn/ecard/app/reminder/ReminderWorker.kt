package vn.ecard.app.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import vn.ecard.app.MainActivity
import vn.ecard.app.R
import vn.ecard.app.data.EventRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Mỗi ngày (khoảng 8h sáng) kiểm tra lịch và gửi thông báo nhắc tạo thiệp. */
class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        checkAndNotify(applicationContext)
        return Result.success()
    }

    companion object {
        const val CHANNEL = "ecard_reminders"
        const val EXTRA_OCCASION = "occasion"
        const val EXTRA_EVENT_NAME = "event_name"
        private const val WORK = "ecard_daily_check"

        fun schedule(ctx: Context) {
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(8, 0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMinutes()
            val req = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
        }

        /** Chạy kiểm tra ngay (dùng khi mở app). */
        fun runNow(ctx: Context) {
            WorkManager.getInstance(ctx).enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
        }

        fun ensureChannel(ctx: Context) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(CHANNEL, "Nhắc sự kiện", NotificationManager.IMPORTANCE_DEFAULT)
            ch.description = "Nhắc trước các ngày Lễ, Tết, sinh nhật, ngày giỗ để tạo thiệp"
            nm.createNotificationChannel(ch)
        }

        fun checkAndNotify(ctx: Context) {
            ensureChannel(ctx)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return
            val repo = EventRepository(ctx)
            val today = LocalDate.now()
            repo.load().filter { it.enabled }.forEach { e ->
                val next = e.nextDate(today)
                val left = e.daysLeft(today).toInt()
                if (left !in e.remindDays) return@forEach
                val key = "${e.id}_${next}_$left"
                if (repo.wasNotified(key)) return@forEach

                val title = when (left) {
                    0 -> "Hôm nay: ${e.type.icon} ${e.name}"
                    1 -> "Ngày mai: ${e.type.icon} ${e.name}"
                    else -> "Còn $left ngày: ${e.type.icon} ${e.name}"
                }
                val body = "${e.dateLabel()} – ngày ${next.dayOfMonth}/${next.monthValue}/${next.year}. Chạm để tạo thiệp ngay."
                val intent = Intent(ctx, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_OCCASION, e.occasion.name)
                    putExtra(EXTRA_EVENT_NAME, e.name)
                }
                val pi = PendingIntent.getActivity(
                    ctx, e.id.hashCode(), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val n = NotificationCompat.Builder(ctx, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build()
                try {
                    NotificationManagerCompat.from(ctx).notify(key.hashCode(), n)
                    repo.markNotified(key)
                } catch (_: SecurityException) {
                }
            }
        }
    }
}
