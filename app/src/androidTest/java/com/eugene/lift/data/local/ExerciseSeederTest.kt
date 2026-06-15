package com.eugene.lift.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eugene.lift.R
import com.eugene.lift.data.repository.ExerciseRepositoryImpl
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseSeederTest {

    private lateinit var database: AppDatabase
    private lateinit var seeder: ExerciseSeeder
    private lateinit var repository: ExerciseRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExerciseRepositoryImpl(database.exerciseDao(), context)
        seeder = ExerciseSeeder(repository, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun populateIfEmpty_insertsCatalogOnlyOnce() = runBlocking {
        seeder.populateIfEmpty()
        val firstCount = repository.getCount()

        seeder.populateIfEmpty()
        val secondCount = repository.getCount()

        assertTrue(firstCount > 0)
        assertEquals(firstCount, secondCount)
    }

    @Test
    fun populateIfEmpty_preservesSeedBodyParts() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val benchName = context.getString(R.string.seed_bench_press)

        seeder.populateIfEmpty()

        val bench = repository.getExercises().first().firstOrNull { it.name == benchName }

        assertNotNull(bench)
        assertEquals(
            setOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS),
            bench?.bodyParts?.toSet()
        )
        assertEquals("seed_bench_press", bench?.seedKey)
    }

    @Test
    fun populateIfEmpty_doesNotOverwriteExistingCatalog() = runBlocking {
        val customExercise = Exercise(
            id = "custom-local-id",
            name = "Existing Custom Exercise",
            category = ExerciseCategory.DUMBBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Keep existing catalog intact.",
            imagePath = null,
            bodyParts = listOf(BodyPart.BICEPS)
        )
        repository.saveExercise(customExercise)

        seeder.populateIfEmpty()

        val exercises = repository.getExercises().first()

        assertEquals(1, repository.getCount())
        assertEquals("custom-local-id", exercises.single().id)
        assertEquals("Existing Custom Exercise", exercises.single().name)
        assertEquals(setOf(BodyPart.BICEPS), exercises.single().bodyParts.toSet())
    }
}
