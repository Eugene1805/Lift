package com.eugene.lift.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import java.util.UUID

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: ExerciseCategory,
    val measureType: MeasureType,
    @Ignore // <--- Room ignora esto, no crea columna. Nosotros lo llenamos manual en el Repo.
    val bodyParts: List<BodyPart> = emptyList()
){
    constructor(id: String, name: String, category: ExerciseCategory, measureType: MeasureType)
            : this(id, name, category, measureType, emptyList())
}