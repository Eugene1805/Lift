package com.eugene.lift.data.local

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
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
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ExerciseSeederTest {

    private lateinit var database: AppDatabase
    private lateinit var seeder: ExerciseSeeder
    private lateinit var repository: ExerciseRepositoryImpl
    private lateinit var settingsDataSource: SettingsDataSource

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsDataSource = SettingsDataSource(context)
        runBlocking {
            settingsDataSource.setLanguageCode("en")
        }
        repository = ExerciseRepositoryImpl(database.exerciseDao(), settingsDataSource, context)
        seeder = ExerciseSeeder(repository, settingsDataSource, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun localizedString(context: Context, languageCode: String, @StringRes resId: Int): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(languageCode))
        return context.createConfigurationContext(config).getString(resId)
    }

    @Test
    fun populateIfEmpty_insertsCatalogOnlyOnce() = runBlocking {
        seeder.populateIfEmpty()
        val firstCount = repository.getCount()
        val firstCatalog = repository.getExercises().first()

        seeder.populateIfEmpty()
        val secondCount = repository.getCount()

        assertTrue(firstCount > 0)
        assertEquals(firstCount, secondCount)
        assertTrue(firstCatalog.none { it.seedKey == null })
        assertEquals(firstCatalog.size, firstCatalog.mapNotNull { it.seedKey }.distinct().size)
    }

    @Test
    fun populateIfEmpty_preservesSeedBodyParts() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val benchName = localizedString(context, "en", R.string.seed_bench_press)

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

        assertTrue(repository.getCount() > 1)
        val custom = exercises.first { it.id == "custom-local-id" }
        assertEquals("Existing Custom Exercise", custom.name)
        assertEquals(setOf(BodyPart.BICEPS), custom.bodyParts.toSet())
    }

    @Test
    fun populateIfEmpty_addsOnlyMissingSeedKeys() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val seededBench = Exercise(
            id = "seed-bench-id",
            name = localizedString(context, "en", R.string.seed_bench_press),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = localizedString(context, "en", R.string.seed_bench_desc),
            imagePath = "bench_press",
            bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS),
            seedKey = "seed_bench_press"
        )
        repository.saveExercise(seededBench)

        seeder.populateIfEmpty()

        val exercises = repository.getExercises().first()
        assertEquals(1, exercises.count { it.seedKey == "seed_bench_press" })
        assertTrue(exercises.any { it.seedKey == "seed_step_up" })
        assertTrue(exercises.any { it.seedKey == "seed_pendulum_squat" })
    }

    @Test
    fun populateIfEmpty_addsPendulumSquatWithoutImage() = runBlocking {
        seeder.populateIfEmpty()

        val pendulumSquat = repository.getExercises().first()
            .single { it.seedKey == "seed_pendulum_squat" }

        assertEquals(ExerciseCategory.MACHINE, pendulumSquat.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, pendulumSquat.measureType)
        assertEquals(null, pendulumSquat.imagePath)
        assertEquals(
            setOf(BodyPart.QUADRICEPS, BodyPart.GLUTES, BodyPart.HAMSTRINGS),
            pendulumSquat.bodyParts.toSet()
        )
    }

    @Test
    fun populateIfEmpty_doesNotDuplicateSeededNameWhenSeedKeyIsMissing() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val benchName = localizedString(context, "en", R.string.seed_bench_press)
        val benchDescription = localizedString(context, "en", R.string.seed_bench_desc)
        repository.saveExercise(
            Exercise(
                id = "legacy-bench-id",
                name = benchName,
                category = ExerciseCategory.BARBELL,
                measureType = MeasureType.REPS_AND_WEIGHT,
                instructions = benchDescription,
                imagePath = "bench_press",
                bodyParts = listOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS),
                seedKey = null
            )
        )

        seeder.populateIfEmpty()

        val exercises = repository.getExercises().first()
        assertEquals(1, exercises.count { it.name == benchName })
    }

    @Test
    fun seededExercises_relocalizeWhenLanguageChanges() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        seeder.populateIfEmpty()

        val englishName = repository.getExercises().first()
            .first { it.seedKey == "seed_bench_press" }
            .name

        settingsDataSource.setLanguageCode("es")

        val spanishName = repository.getExercises().first()
            .first { it.seedKey == "seed_bench_press" }
            .name

        assertEquals(localizedString(context, "en", R.string.seed_bench_press), englishName)
        assertEquals(localizedString(context, "es", R.string.seed_bench_press), spanishName)
    }
}
