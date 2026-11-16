package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.usecase.participation.CheckInResult
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class QrCheckInTest {
    @Test fun generatedQrCanBeDecodedAndDoesNotContainProfileData() {
        val code = "AKJ-ABC123"
        val matrix = QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, 300, 300)
        val pixels = IntArray(300 * 300) { i -> if (matrix[i % 300, i / 300]) 0xff000000.toInt() else 0xffffffff.toInt() }
        val decoded = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(RGBLuminanceSource(300, 300, pixels))))
        assertEquals(code, decoded.text)
    }

    @Test fun twoUseCaseInstancesAndRepeatedAttemptsOnlyAwardOnePrize() = runTest {
        val repos = TestRepositories()
        val event = repos.events.findPublicEvent("ciclopaseo")!!
        val before = repos.pointsOf(DEMO_USER)
        val a = repos.useCases.checkIn
        val b = TestUseCases(repos.events, repos.users, repos.notifications).checkIn
        val results = listOf(async { a(event.checkInCode, DEMO_USER) }, async { b(event.checkInCode, DEMO_USER) }).map { it.await() }
        assertEquals(1, results.count { it is CheckInResult.Success })
        assertEquals(1, results.count { it is CheckInResult.AlreadyCheckedIn })
        assertEquals(before + 15, repos.pointsOf(DEMO_USER))
        assertEquals(1, repos.users.pointsHistory.value.count { it.reason == PointsReason.CHECK_IN && it.eventTitle == event.title })
        assertTrue(a(event.checkInCode, DEMO_USER) is CheckInResult.AlreadyCheckedIn)
        assertEquals(before + 15, repos.pointsOf(DEMO_USER))
    }

    @Test fun wrongEventDoesNotChangeAttendanceBalanceOrHistory() = runTest {
        val repos = TestRepositories()
        val event = repos.events.findPublicEvent("ciclopaseo")!!
        val before = repos.pointsOf(DEMO_USER)
        val history = repos.users.pointsHistory.value
        assertEquals(CheckInResult.WrongEvent, repos.useCases.checkIn(event.checkInCode, DEMO_USER, "feria-cultural"))
        assertEquals(before, repos.pointsOf(DEMO_USER))
        assertEquals(history, repos.users.pointsHistory.value)
    }
}
