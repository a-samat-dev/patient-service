package kz.samat.patientservice.util

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate

/**
 * LocalDate converter for GSON library
 *
 * Created by Samat Abibulla on 2023-01-09
 */
class LocalDateConverter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    override fun serialize(p0: LocalDate?, p1: Type?, p2: JsonSerializationContext?): JsonElement =
        JsonPrimitive(p0.toString())

    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): LocalDate =
        LocalDate.parse(p0?.asString)
}