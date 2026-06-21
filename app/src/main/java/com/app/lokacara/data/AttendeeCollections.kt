package com.app.lokacara.data

import com.app.lokacara.data.remote.dto.AttendeeDto

fun mergeAttendeesById(existing: List<AttendeeDto>, incoming: List<AttendeeDto>): List<AttendeeDto> {
    val existingIds = existing.asSequence().map(AttendeeDto::id).toHashSet()
    return existing + incoming.filterNot { it.id in existingIds }
}

fun replaceAttendeeById(existing: List<AttendeeDto>, updated: AttendeeDto): List<AttendeeDto> {
    return existing.map { attendee -> if (attendee.id == updated.id) updated else attendee }
}
