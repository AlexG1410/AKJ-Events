package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.edu.uniquindio.akjevents.core.theme.WarmText
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/** Código QR real (generado con ZXing) que se puede leer con cualquier lector. */
@Composable
fun QrCode(content: String, modifier: Modifier = Modifier, color: Color = WarmText) {
    // Con tamaño 0 ZXing devuelve la matriz mínima: un módulo por celda
    val matrix = remember(content) {
        QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, mapOf(EncodeHintType.MARGIN to 1))
    }
    Canvas(modifier = modifier.aspectRatio(1f).semantics { contentDescription = "Código QR $content" }) {
        val cell = size.width / matrix.width
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                if (matrix[x, y]) {
                    // Medio píxel extra para que no queden líneas entre módulos vecinos
                    drawRect(color, topLeft = Offset(x * cell, y * cell), size = Size(cell + 0.5f, cell + 0.5f))
                }
            }
        }
    }
}
