package com.eugene.lift.data.remote

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eugene.lift.core.util.Logger
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.dao.TemplateDao
import com.eugene.lift.data.local.dao.WorkoutDao
import com.eugene.lift.data.local.entity.SessionExerciseEntity
import com.eugene.lift.data.local.entity.TemplateExerciseEntity
import com.eugene.lift.data.local.entity.WorkoutSessionEntity
import com.eugene.lift.data.local.entity.WorkoutTemplateEntity
import com.eugene.lift.data.mapper.toCrossRefs
import com.eugene.lift.data.mapper.toEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class WgerExerciseCatalogSyncerTest {

    private lateinit var database: AppDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var templateDao: TemplateDao
    private lateinit var workoutDao: WorkoutDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseDao = database.exerciseDao()
        templateDao = database.templateDao()
        workoutDao = database.workoutDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun roomExerciseSyncWriter_rollsBackAllWritesWhenPersistenceFails() = runBlocking {
        val firstExercise = remoteExercise(id = "local-1", remoteId = 101, name = "Bench Press")
        val secondExercise = remoteExercise(id = "local-2", remoteId = 102, name = "Squat")
        val writer = RoomExerciseSyncWriter(
            database = database,
            itemPersister = object : ExerciseSyncItemPersister {
                private var writes = 0

                override suspend fun persist(exercise: Exercise) {
                    writes += 1
                    if (writes == 2) {
                        throw IllegalStateException("boom")
                    }
                    exerciseDao.saveExerciseComplete(exercise.toEntity(), exercise.toCrossRefs())
                }
            }
        )

        try {
            writer.applySyncedExercises(listOf(firstExercise, secondExercise))
        } catch (_: IllegalStateException) {
        }

        assertEquals(0, exerciseDao.getExerciseCount())
        assertEquals(emptyList<ExerciseEntitySnapshot>(), snapshotExercises())
    }

    @Test
    fun syncExercises_preservesReferencedExerciseIds() = runBlocking {
        val existingExercise = remoteExercise(
            id = "existing-local-id",
            remoteId = 42,
            name = "Old Bench Press",
            bodyParts = listOf(BodyPart.CHEST)
        )
        exerciseDao.saveExerciseComplete(existingExercise.toEntity(), existingExercise.toCrossRefs())

        templateDao.saveTemplateComplete(
            template = WorkoutTemplateEntity(
                id = "template-1",
                name = "Push Day",
                notes = "",
                isArchived = false,
                lastPerformedAt = null
            ),
            exercises = listOf(
                TemplateExerciseEntity(
                    id = "template-ex-1",
                    templateId = "template-1",
                    exerciseId = existingExercise.id,
                    orderIndex = 0,
                    targetSets = 3,
                    targetReps = "8-10",
                    restTimerSeconds = 120,
                    note = ""
                )
            )
        )

        workoutDao.saveSessionComplete(
            session = WorkoutSessionEntity(
                id = "session-1",
                templateId = "template-1",
                name = "Push Day",
                date = LocalDateTime.now(),
                durationSeconds = 1200
            ),
            exercises = listOf(
                SessionExerciseEntity(
                    id = "session-ex-1",
                    sessionId = "session-1",
                    exerciseId = existingExercise.id,
                    orderIndex = 0
                )
            ),
            sets = emptyList()
        )

        val syncer = WgerExerciseCatalogSyncer(
            remoteDataSource = object : ExerciseRemoteDataSource {
                override suspend fun syncExercises(): List<RemoteExercisePayload> {
                    return listOf(
                        RemoteExercisePayload(
                            remoteId = 42,
                            name = "Bench Press",
                            description = "Updated description",
                            categoryName = "Strength",
                            primaryImageUrl = "https://example.com/bench.png",
                            equipmentNames = listOf("Bench"),
                            primaryMuscleNames = listOf("Chest"),
                            secondaryMuscleNames = listOf("Triceps", "Front deltoid")
                        )
                    )
                }
            },
            exerciseDao = exerciseDao,
            exerciseSyncWriter = RoomExerciseSyncWriter(database, RoomExerciseSyncItemPersister(exerciseDao)),
            logger = TestLogger()
        )

        val syncedCount = syncer.syncExercises()
        val synced = exerciseDao.getExerciseByRemoteId(42)
        val template = templateDao.getTemplateById("template-1").first()
        val session = workoutDao.getSessionById("session-1").first()

        assertEquals(1, syncedCount)
        assertNotNull(synced)
        assertEquals("existing-local-id", synced?.id)
        assertEquals("Bench Press", synced?.name)
        assertEquals(ExerciseSource.WGER, synced?.source)
        assertEquals("existing-local-id", template?.exercises?.single()?.exercise?.id)
        assertEquals("existing-local-id", session?.exercises?.single()?.exercise?.id)
        assertEquals(
            setOf(BodyPart.CHEST, BodyPart.TRICEPS, BodyPart.FRONT_DELTS),
            template?.exercises?.single()?.bodyPartRefs?.map { it.bodyPart }?.toSet()
        )
    }

    @Test
    fun syncExercises_doesNotMergeByNameWhenLocalExerciseIsReferenced() = runBlocking {
        val localSeedExercise = Exercise(
            id = "seed-local-id",
            name = "Bench Press",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Local seed instructions",
            imagePath = null,
            bodyParts = listOf(BodyPart.CHEST),
            source = ExerciseSource.LOCAL
        )
        exerciseDao.saveExerciseComplete(localSeedExercise.toEntity(), localSeedExercise.toCrossRefs())

        templateDao.saveTemplateComplete(
            template = WorkoutTemplateEntity(
                id = "template-name-match",
                name = "Chest Day",
                notes = "",
                isArchived = false,
                lastPerformedAt = null
            ),
            exercises = listOf(
                TemplateExerciseEntity(
                    id = "template-name-match-ex",
                    templateId = "template-name-match",
                    exerciseId = localSeedExercise.id,
                    orderIndex = 0,
                    targetSets = 4,
                    targetReps = "6-8",
                    restTimerSeconds = 150,
                    note = ""
                )
            )
        )

        workoutDao.saveSessionComplete(
            session = WorkoutSessionEntity(
                id = "session-name-match",
                templateId = "template-name-match",
                name = "Chest Day",
                date = LocalDateTime.now(),
                durationSeconds = 1800
            ),
            exercises = listOf(
                SessionExerciseEntity(
                    id = "session-name-match-ex",
                    sessionId = "session-name-match",
                    exerciseId = localSeedExercise.id,
                    orderIndex = 0
                )
            ),
            sets = emptyList()
        )

        val syncer = WgerExerciseCatalogSyncer(
            remoteDataSource = object : ExerciseRemoteDataSource {
                override suspend fun syncExercises(): List<RemoteExercisePayload> {
                    return listOf(
                        RemoteExercisePayload(
                            remoteId = 420,
                            name = "bench-press",
                            description = "Remote catalog description",
                            categoryName = "Strength",
                            primaryImageUrl = "https://example.com/bench.png",
                            equipmentNames = listOf("Barbell"),
                            primaryMuscleNames = listOf("Chest"),
                            secondaryMuscleNames = listOf("Triceps")
                        )
                    )
                }
            },
            exerciseDao = exerciseDao,
            exerciseSyncWriter = RoomExerciseSyncWriter(database, RoomExerciseSyncItemPersister(exerciseDao)),
            logger = TestLogger()
        )

        val syncedCount = syncer.syncExercises()
        val template = templateDao.getTemplateById("template-name-match").first()
        val session = workoutDao.getSessionById("session-name-match").first()
        val localAfter = exerciseDao.getExerciseById(localSeedExercise.id).first()
        val remoteAfter = exerciseDao.getExerciseByRemoteId(420)

        assertEquals(1, syncedCount)
        assertEquals(2, exerciseDao.getExerciseCount())
        assertEquals("seed-local-id", template?.exercises?.single()?.exercise?.id)
        assertEquals("seed-local-id", session?.exercises?.single()?.exercise?.id)
        assertEquals(ExerciseSource.LOCAL, localAfter?.exercise?.source)
        assertEquals("seed-local-id", localAfter?.exercise?.id)
        assertNotNull(remoteAfter)
        assertEquals("bench-press", remoteAfter?.name)
        assertEquals(ExerciseSource.WGER, remoteAfter?.source)
    }

    private suspend fun snapshotExercises(): List<ExerciseEntitySnapshot> {
        return exerciseDao.getExercisesByRemoteIds(listOf(101, 102)).map { entity ->
            ExerciseEntitySnapshot(
                id = entity.id,
                remoteId = entity.remoteId
            )
        }
    }

    private fun remoteExercise(
        id: String,
        remoteId: Int,
        name: String,
        bodyParts: List<BodyPart> = listOf(BodyPart.CHEST, BodyPart.TRICEPS)
    ): Exercise {
        return Exercise(
            id = id,
            name = name,
            category = ExerciseCategory.MACHINE,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "desc",
            imagePath = null,
            bodyParts = bodyParts,
            remoteId = remoteId,
            source = ExerciseSource.WGER
        )
    }

    private data class ExerciseEntitySnapshot(
        val id: String,
        val remoteId: Int?
    )

    private class TestLogger : Logger {
        override fun log(throwable: Throwable) = Unit
    }
}
