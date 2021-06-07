/*
 * Copyright 2019-2021 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package kotlinx.datetime.test

import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.test.*

class UtcOffsetTest {

    companion object {
        val invalidUtcOffsetStrings = listOf(
            "", *('A'..'Y').map { it.toString() }.toTypedArray(), "ZZ",
            "0", "+0:00", "+00:0", "+0:0",
            "+000", "+00000",
            "+0:00:00", "+00:0:00", "+00:00:0", "+0:0:0", "+0:0:00", "+00:0:0", "+0:00:0",
            "1", "+01_00", "+01;00", "+01@00", "+01:AA",
            "+19", "+19:00", "+18:01", "+18:00:01", "+1801", "+180001",
            "-0:00", "-00:0", "-0:0",
            "-000", "-00000",
            "-0:00:00", "-00:0:00", "-00:00:0", "-0:0:0", "-0:0:00", "-00:0:0", "-0:00:0",
            "-19", "-19:00", "-18:01", "-18:00:01", "-1801", "-180001",
            "-01_00", "-01;00", "-01@00", "-01:AA",
            "@01:00")

        val fixedOffsetTimeZoneIds = listOf(
            "UTC", "UTC+0", "GMT+01", "UT-01", "Etc/UTC"
        )

        val offsetSecondsRange = -18 * 60 * 60 .. +18 * 60 * 60
    }

    @Test
    fun invalidUtcOffsetStrings() {
        for (v in invalidUtcOffsetStrings) {
            assertFailsWith<DateTimeFormatException>("Should fail: $v") { UtcOffset.parse(v) }
        }
        for (v in fixedOffsetTimeZoneIds) {
            assertFailsWith<DateTimeFormatException>("Time zone name should not be parsed as UtcOffset: $v") { UtcOffset.parse(v) }
        }
    }

    @Test
    fun parseAllValidValues() {
        fun Int.pad() = toString().padStart(2, '0')
        fun check(offsetSeconds: Int, offsetString: String) {
            val offset = UtcOffset.parse(offsetString)
            if (offsetSeconds != offset.totalSeconds) {
                fail("Expected string $offsetString to be parsed as $offset and have $offsetSeconds offset, got ${offset.totalSeconds}")
            }
        }

        for (offsetSeconds in offsetSecondsRange) {
            val sign = when {
                offsetSeconds < 0 -> "-"
                else -> "+"
            }
            val hours = abs(offsetSeconds / 60 / 60)
            val minutes = abs(offsetSeconds / 60 % 60)
            val seconds = abs(offsetSeconds % 60)


            check(offsetSeconds, "$sign${hours.pad()}:${minutes.pad()}:${seconds.pad()}")
            check(offsetSeconds, "$sign${hours.pad()}${minutes.pad()}${seconds.pad()}")
            if (seconds == 0) {
                check(offsetSeconds, "$sign${hours.pad()}:${minutes.pad()}")
                check(offsetSeconds, "$sign${hours.pad()}${minutes.pad()}")
                if (minutes == 0) {
                    check(offsetSeconds, "$sign${hours.pad()}")
                    check(offsetSeconds, "$sign$hours")
                }
            }
        }
        check(0, "+00:00")
        check(0, "-00:00")
        check(0, "+0")
        check(0, "-0")
        check(0, "Z")
    }

    @Test
    fun asTimeZone() {
        val offset = UtcOffset.parse("+01:20:30")
        val timeZone = offset.asTimeZone()
        assertIs<FixedOffsetTimeZone>(timeZone)
        assertEquals(offset, timeZone.utcOffset)
    }
}