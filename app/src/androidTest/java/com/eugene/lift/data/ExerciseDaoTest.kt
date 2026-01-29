package com.eugene.lift.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.entity.ExerciseBodyPartCrossRef
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test for ExerciseDao
 * Tests Room database operations with actual Android dependencies
 * Uses in-memory database for fast, isolated tests
 */
@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // InMemoryDatabaseBuilder creates a DB in RAM
        // Destroyed after test completes - perfect for testing
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Only for tests!
            .build()
        dao = db.exerciseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertExercise_withBodyParts_thenRetrieveById_returnsCorrectData() = runTest {
        // GIVEN: An exercise with multiple body parts
        val exercise = ExerciseEntity(
            name = "Press Militar",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Press overhead",
            imagePath = null
        )

        val bodyPartCrossRefs = listOf(
            ExerciseBodyPartCrossRef(exercise.id, BodyPart.FRONT_DELTS),
            ExerciseBodyPartCrossRef(exercise.id, BodyPart.SIDE_DELTS)
        )

        // WHEN: Insert exercise and body part relationships
        dao.insertExercise(exercise)
        dao.insertCrossRefs(bodyPartCrossRefs)

        // THEN: Retrieve and verify data
        val result = dao.getExerciseById(exercise.id).first()

        assertNotNull(result)
        assertEquals("Press Militar", result?.exercise?.name)
        assertEquals(ExerciseCategory.BARBELL, result?.exercise?.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, result?.exercise?.measureType)

        // Verify body parts (returned as comma-separated string)
        val bodyParts = result?.bodyParts?.split(",") ?: emptyList()
        assertEquals(2, bodyParts.size)
    }

    @Test
    fun insertExercise_testEnumConverters() = runTest {
        // GIVEN: Exercise with various enum types
        val exercise = ExerciseEntity(
            name = "Bench Press",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Lower to chest",
            imagePath = null
        )

        val bodyPartCrossRefs = listOf(
            ExerciseBodyPartCrossRef(exercise.id, BodyPart.CHEST),
            ExerciseBodyPartCrossRef(exercise.id, BodyPart.TRICEPS)
        )

        // WHEN: Insert data
        dao.insertExercise(exercise)
        dao.insertCrossRefs(bodyPartCrossRefs)

        // THEN: Verify enum converters work correctly
        val result = dao.getExerciseById(exercise.id).first()

        assertNotNull(result)
        assertEquals(ExerciseCategory.BARBELL, result?.exercise?.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, result?.exercise?.measureType)

        // Verify body parts are stored and retrieved correctly
        val bodyParts = result?.bodyParts?.split(",") ?: emptyList()
        assert(bodyParts.isNotEmpty())
    }

    @Test
    fun getAllExercises_returnsAllInsertedExercises() = runTest {
        // GIVEN: Multiple exercises
        val exercise1 = ExerciseEntity(
            name = "Squat",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )
        val exercise2 = ExerciseEntity(
            name = "Pull-ups",
            category = ExerciseCategory.BODYWEIGHT,
            measureType = MeasureType.REPS_ONLY,
            instructions = "",
            imagePath = null
        )

        // WHEN: Insert exercises
        dao.insertExercise(exercise1)
        dao.insertExercise(exercise2)

        // THEN: Verify all exercises are returned
        val allExercises = dao.getAllExercises().first()
        assertEquals(2, allExercises.size)
    }

    @Test
    fun deleteExercise_removesExerciseFromDatabase() = runTest {
        // GIVEN: An exercise inserted into database
        val exercise = ExerciseEntity(
            name = "Deadlift",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )
        dao.insertExercise(exercise)

        // WHEN: Delete the exercise
        dao.deleteExerciseComplete(exercise.id)

        // THEN: Exercise should not be retrievable
        val result = dao.getExerciseById(exercise.id).first()
        assertEquals(null, result)
    }

    @Test
    fun getExerciseCount_returnsCorrectCount() = runTest {
        // GIVEN: Insert some exercises
        val exercise1 = ExerciseEntity(
            name = "Exercise 1",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT
        )
        val exercise2 = ExerciseEntity(
            name = "Exercise 2",
            category = ExerciseCategory.DUMBBELL,
            measureType = MeasureType.REPS_AND_WEIGHT
        )

        // WHEN: Insert exercises
        dao.insertExercise(exercise1)
        dao.insertExercise(exercise2)

        // THEN: Count should be correct
        val count = dao.getExerciseCount()
        assertEquals(2, count)
    }
}