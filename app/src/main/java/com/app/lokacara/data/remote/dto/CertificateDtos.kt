package com.app.lokacara.data.remote.dto

data class CertificateTemplateUploadResponse(
    val message: String,
    val template_path: String
)

data class OrganizerCertificateStateResponse(
    val event: OrganizerCertificateEventDto,
    val is_eligible: Boolean,
    val has_template: Boolean,
    val issued_count: Int,
    val last_issued_at: String? = null,
    val status: String,
    val layout: OrganizerCertificateLayoutDto
)

data class OrganizerCertificateEventDto(val id: Long, val title: String, val end_datetime: String)
data class OrganizerCertificateLayoutDto(
    val font_family: String? = null,
    val font_color: String? = null,
    val font_size: String? = null,
    val x_pos: Float? = null,
    val is_x_center: Boolean? = null,
    val y_pos: Float? = null,
    val is_y_center: Boolean? = null,
    val max_width: Float? = null,
    val max_height: Float? = null
)

data class DistributeCertificatesRequest(
    val template_path: String,
    val font_family: String,
    val font_color: String,
    val font_size: String,
    val x_pos: Float,
    val is_x_center: Boolean,
    val y_pos: Float,
    val is_y_center: Boolean
)

data class CertificateLayoutConfig(
    val fontFamily: String,
    val fontColor: String,
    val fontSize: String,
    val xPosition: Float,
    val isXCentered: Boolean,
    val yPosition: Float,
    val isYCentered: Boolean
) {
    fun toDistributeRequest(templatePath: String) = DistributeCertificatesRequest(
        template_path = templatePath,
        font_family = fontFamily,
        font_color = fontColor,
        font_size = fontSize,
        x_pos = xPosition,
        is_x_center = isXCentered,
        y_pos = yPosition,
        is_y_center = isYCentered
    )
}
