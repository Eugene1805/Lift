package com.eugene.lift.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.exerciseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveExerciseComplete_roundTripsExerciseWithBodyParts() = runBlocking {
        val exercise = ExerciseEntity(
            id = "local-1",
            name = "Bench Press",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Press"
        )

        dao.saveExerciseComplete(
            exercise = exercise,
            refs = listOf(
                ExerciseBodyPartCrossRef("local-1", BodyPart.CHEST),
                ExerciseBodyPartCrossRef("local-1", BodyPart.TRICEPS)
            )
        )

        val stored = dao.getExerciseById("local-1").first()

        assertNotNull(stored)
        assertEquals("local-1", stored?.exercise?.id)
        assertEquals("Bench Press", stored?.exercise?.name)
        assertEquals(setOf("CHEST", "TRICEPS"), stored?.bodyParts?.split(",")?.toSet())
    }

    @Test
    fun getExercisesWithoutImage_returnsOnlyMissingImages() = runBlocking {
        dao.upsertExercises(
            listOf(
                ExerciseEntity(
                    id = "local-1",
                    name = "Bench Press",
                    category = ExerciseCategory.BARBELL,
                    measureType = MeasureType.REPS_AND_WEIGHT,
                    imagePath = null
                ),
                ExerciseEntity(
                    id = "local-2",
                    name = "Squat",
                    category = ExerciseCategory.BARBELL,
                    measureType = MeasureType.REPS_AND_WEIGHT,
                    imagePath = "back_squat"
                )
            )
        )

        val missing = dao.getExercisesWithoutImage()

        assertEquals(1, missing.size)
        assertEquals("local-1", missing.single().id)
    }
}
