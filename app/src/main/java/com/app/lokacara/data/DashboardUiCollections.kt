package com.app.lokacara.data

import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.NotificationItem
import com.app.lokacara.model.NotificationType

fun filterNotifications(items: List<NotificationItem>, tabIndex: Int): List<NotificationItem> {
    val type = if (tabIndex == 0) NotificationType.SOCIAL else NotificationType.SYSTEM
    return items.filter { it.type == type }
}

fun markCertificateDownloaded(
    items: List<CertificateData>,
    certificateId: String,
    filePath: String?
): List<CertificateData> {
    return items.map { certificate ->
        if (certificate.id == certificateId) certificate.copy(filePath = filePath) else certificate
    }
}
