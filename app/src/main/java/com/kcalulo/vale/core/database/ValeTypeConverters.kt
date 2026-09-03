package com.kcalulo.vale.core.database

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

class ValeTypeConverters {

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun localDateToLong(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun longToLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)
}
