package com.app.lokacara.data.remote.dto

data class CertificateTemplateUploadResponse(
    val message: String,
    val template_path: String
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
