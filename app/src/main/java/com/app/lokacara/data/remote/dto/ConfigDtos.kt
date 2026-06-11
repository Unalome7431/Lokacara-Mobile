package com.app.lokacara.data.remote.dto

data class ConfigTabsResponse(
    val tickets_tabs: List<TabEntry> = emptyList(),
    val notification_tabs: List<TabEntry> = emptyList(),
    val settings_sections: List<TabEntry> = emptyList()
)

data class TabEntry(
    val key: String,
    val label: String
)
