package com.app.lokacara.data

fun completedEventRequirements(
    hasName: Boolean,
    hasCategory: Boolean,
    hasOrganizer: Boolean,
    hasContact: Boolean,
    hasSchedule: Boolean,
    hasLocation: Boolean,
    hasDescription: Boolean,
    hasPrice: Boolean,
    hasValidCapacity: Boolean
): Int = listOf(
    hasName,
    hasCategory,
    hasOrganizer,
    hasContact,
    hasSchedule,
    hasLocation,
    hasDescription,
    hasPrice,
    hasValidCapacity
).count { it }
