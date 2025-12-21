package com.eugene.lift.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.eugene.lift.domain.model.BodyPart

data class ExerciseWithBodyParts(
    @Embedded val exercise: ExerciseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "bodyPart",
        associateBy = Junction(
            value = ExerciseBodyPartCrossRef::class,
            parentColumn = "exerciseId",
            entityColumn = "bodyPart"
        )
    )
    val bodyParts: List<BodyPart> // Room llenará esto mágicamente
)