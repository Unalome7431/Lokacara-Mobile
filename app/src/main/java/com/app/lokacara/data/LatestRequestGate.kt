package com.app.lokacara.data

class LatestRequestGate {
    private var generation = 0L

    @Synchronized
    fun next(): Long = ++generation

    @Synchronized
    fun isLatest(token: Long): Boolean = token == generation
}
