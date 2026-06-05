package com.eugene.lift.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.ExerciseSource
import com.eugene.lift.domain.model.MeasureType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun upsertAndFindByRemoteId_preservesRemoteMetadata() = runBlocking {
        val remoteExercise = ExerciseEntity(
            id = "local-remote-1",
            name = "Bench Press",
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "Press",
            remoteId = 101,
            source = ExerciseSource.WGER,
            lastSyncedAt = 123456789L,
            syncVersion = 1
        )

        dao.upsertExercise(remoteExercise)

        val stored = dao.getExerciseByRemoteId(101)

        assertNotNull(stored)
        assertEquals("local-remote-1", stored?.id)
        assertEquals(ExerciseSource.WGER, stored?.source)
        assertEquals(123456789L, stored?.lastSyncedAt)
        assertEquals(1, stored?.syncVersion)
    }

    @Test
    fun bulkLookupByRemoteIds_returnsOnlyMatchingExercises() = runBlocking {
        dao.upsertExercises(
            listOf(
                ExerciseEntity(
                    id = "local-1",
                    name = "Bench Press",
                    category = ExerciseCategory.BARBELL,
                    measureType = MeasureType.REPS_AND_WEIGHT,
                    remoteId = 101,
                    source = ExerciseSource.WGER
                ),
                ExerciseEntity(
                    id = "local-2",
                    name = "Squat",
                    category = ExerciseCategory.BARBELL,
                    measureType = MeasureType.REPS_AND_WEIGHT,
                    remoteId = 102,
                    source = ExerciseSource.WGER
                ),
                ExerciseEntity(
                    id = "local-3",
                    name = "Custom Row",
                    category = ExerciseCategory.DUMBBELL,
                    measureType = MeasureType.REPS_AND_WEIGHT
                )
            )
        )

        val matches = dao.getExercisesByRemoteIds(listOf(102, 999, 101))
            .associateBy { it.remoteId }

        assertEquals(2, matches.size)
        assertEquals("local-1", matches[101]?.id)
        assertEquals("local-2", matches[102]?.id)
        assertNull(matches[999])
    }
}
