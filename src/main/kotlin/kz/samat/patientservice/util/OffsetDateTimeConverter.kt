package kz.samat.patientservice.util

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OffsetDateTimeConverter : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    override fun serialize(p0: OffsetDateTime?, p1: Type?, p2: JsonSerializationContext?): JsonElement {
        if (p0 == null) {
            return JsonPrimitive("")
        }

        return JsonPrimitive(p0.toString())
    }

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): OffsetDateTime {
        if (p0 == null) {
            return OffsetDateTime.now()
        }

        val split: List<String> = p0.asString.split("T")
        val time = split[1].split("+")[0].substring(0, 12)
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val dateTime = LocalDateTime.parse(split[0] + " " + time, formatter)
        val hourAndMinute = getHourAndMinute(time)

        return OffsetDateTime.of(
            dateTime,
            ZoneOffset.ofHoursMinutesSeconds(hourAndMinute[0], hourAndMinute[1], hourAndMinute[2])
        )
    }

    private fun getHourAndMinute(value: String): IntArray {
        val v = value.split(":")

        return intArrayOf(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2].substring(0, 2)))
    }
}