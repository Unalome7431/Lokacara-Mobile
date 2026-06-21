package com.app.lokacara.data

fun completedEventRequirements(
    hasName: Boolean,
    hasCategory: Boolean,
    hasSchedule: Boolean,
    hasLocation: Boolean,
    hasDescription: Boolean,
    hasPrice: Boolean,
    hasValidCapacity: Boolean
): Int = listOf(
    hasName,
    hasCategory,
    hasSchedule,
    hasLocation,
    hasDescription,
    hasPrice,
    hasValidCapacity
).count { it }
