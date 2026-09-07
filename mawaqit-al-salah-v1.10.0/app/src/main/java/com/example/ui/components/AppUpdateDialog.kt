package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.ui.theme.AppColors
import com.example.ui.theme.IslamicGold
import com.example.utils.AppUpdateManager
import com.example.utils.UpdateCheckStatus

@Composable
fun AppUpdateDialog(
    updateStatus: UpdateCheckStatus,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onDownloadNow: (downloadUrl: String, apkFileName: String) -> Unit,
    onCancelDownload: () -> Unit,
    onRetryCheck: () -> Unit
) {
    val context = LocalContext.current

    when (updateStatus) {
        is UpdateCheckStatus.UpdateAvailable -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(IslamicGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "تحديث جديد متوفر" else "New Update Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = IslamicGold.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC)
                                    "الإصدار الجديد: v${updateStatus.latestVersionName}"
                                else
                                    "Latest Version: v${updateStatus.latestVersionName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = IslamicGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        if (updateStatus.releaseNotes.isNotEmpty()) {
                            Text(
                                text = updateStatus.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.textSubtle,
                                textAlign = TextAlign.Center,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC)
                                "انقر على 'تنزيل الآن' لبدء تحميل الإصدار الجديد وتثبيته مباشرة."
                            else
                                "Tap 'Download Now' to start downloading the latest release and install it directly.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.current.textSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDownloadNow(updateStatus.downloadUrl, updateStatus.apkFileName)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("download_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "تنزيل الآن" else "Download Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "لاحقاً" else "Later",
                                color = AppColors.current.textSubtle
                            )
                        }
                    }
                }
            )
        }

        is UpdateCheckStatus.Downloading -> {
            val progress = updateStatus.progressPercent
            val readMb = updateStatus.bytesRead / (1024f * 1024f)
            val totalMb = updateStatus.totalBytes / (1024f * 1024f)

            AlertDialog(
                onDismissRequest = {
                    onCancelDownload()
                    onDismiss()
                },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(AppColors.current.tealAccentLight.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = AppColors.current.tealAccentLight,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "جارِ تنزيل التحديث..." else "Downloading Update...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$progress%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.current.tealAccentLight
                        )

                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AppColors.current.tealAccentLight,
                            trackColor = AppColors.current.tealAccentLight.copy(alpha = 0.2f)
                        )

                        if (totalMb > 0f) {
                            Text(
                                text = String.format("%.1f MB / %.1f MB", readMb, totalMb),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.current.textSubtle
                            )
                        }

                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC)
                                "يرجى الانتظار حتى يكتمل التنزيل بنجاح أو يمكنك إلغاء التنزيل أدناه."
                            else
                                "Please wait for download to complete or tap Cancel below.",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.current.textSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = {
                                onCancelDownload()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_download_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "إلغاء التنزيل" else "Cancel Download",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            )
        }

        is UpdateCheckStatus.DownloadReady -> {
            val apk = updateStatus.apkFile
            AlertDialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "اكتمل التنزيل بنجاح!" else "Download Complete!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC)
                                "تم تنزيل ملف التحديث بنجاح بنسبة 100%. اضغط على زر التثبيت لبدء ترقية التطبيق الآن."
                            else
                                "The update file has downloaded successfully (100%). Tap install to upgrade the app now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.current.textSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                AppUpdateManager.installApk(context, apk)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("install_update_now_button")
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "تثبيت التحديث الآن" else "Install Update Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "إغلاق" else "Close",
                                color = AppColors.current.textSubtle
                            )
                        }
                    }
                }
            )
        }

        is UpdateCheckStatus.PermissionRequired -> {
            val apk = updateStatus.apkFile
            AlertDialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFFFA000).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "مطلوب صلاحية التثبيت والتحديث" else "Install Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC)
                                "يتطلب نظام أندرويد تفعيل صلاحية 'تثبيت التطبيقات غير المعروفة' من إعدادات النظام لتتمكن من تحديث وتثبيت الإصدار الجديد تلقائياً."
                            else
                                "Android requires granting 'Install unknown apps' permission in system settings to update the application directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.current.textSubtle,
                            textAlign = TextAlign.Center
                        )

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA000).copy(alpha = 0.08f))
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC)
                                    "خطوات التفعيل: اضغط على 'منح الصلاحية في الإعدادات' ثم قم بتفعيل خيار 'السماح من هذا المصدر' ثم ارجع إلى التطبيق واضغط تثبيت."
                                else
                                    "Steps: Tap 'Grant Permission', enable 'Allow from this source', then return and tap Install.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.current.textMain,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                AppUpdateManager.openInstallPermissionSettings(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_install_permission_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "منح الصلاحية في الإعدادات" else "Grant Permission in Settings",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                AppUpdateManager.installApk(context, apk)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("try_install_again_button")
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "تثبيت التحديث الآن" else "Install Update Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "إلغاء" else "Cancel",
                                color = AppColors.current.textSubtle
                            )
                        }
                    }
                }
            )
        }

        is UpdateCheckStatus.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                icon = {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "خطأ في التحديث" else "Update Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = updateStatus.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.current.textSubtle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "إغلاق" else "Close",
                                color = AppColors.current.textSubtle
                            )
                        }
                        Button(
                            onClick = onRetryCheck,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IslamicGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "إعادة المحاولة" else "Retry",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }

        else -> {
            // Idle or Checking or UpToDate (handled inline or no dialog needed)
        }
    }
}
