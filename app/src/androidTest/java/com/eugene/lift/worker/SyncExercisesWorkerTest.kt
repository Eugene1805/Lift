package com.eugene.lift.worker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.eugene.lift.core.util.Logger
import com.eugene.lift.data.local.ExerciseBootstrapDataSource
import com.eugene.lift.data.remote.ExerciseCatalogSyncer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SyncExercisesWorkerTest {

    @Test
    fun doWork_returnsRetryOnNetworkFailure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val steps = mutableListOf<String>()
        val worker = TestListenableWorkerBuilder<SyncExercisesWorker>(context)
            .setWorkerFactory(
                TestSyncExercisesWorkerFactory(
                    bootstrapDataSource = object : ExerciseBootstrapDataSource {
                        override suspend fun populateIfEmpty() {
                            steps += "bootstrap"
                        }
                    },
                    exerciseCatalogSyncer = object : ExerciseCatalogSyncer {
                        override suspend fun syncExercises(): Int {
                            steps += "sync"
                            throw IOException("network down")
                        }
                    },
                    logger = NoOpLogger()
                )
            )
            .build()

        val result = worker.startWork().get()

        assertEquals(listOf("bootstrap", "sync"), steps)
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun doWork_bootstrapsBeforeRemoteSync() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val steps = mutableListOf<String>()
        val worker = TestListenableWorkerBuilder<SyncExercisesWorker>(context)
            .setWorkerFactory(
                TestSyncExercisesWorkerFactory(
                    bootstrapDataSource = object : ExerciseBootstrapDataSource {
                        override suspend fun populateIfEmpty() {
                            steps += "bootstrap"
                        }
                    },
                    exerciseCatalogSyncer = object : ExerciseCatalogSyncer {
                        override suspend fun syncExercises(): Int {
                            steps += "sync"
                            return 3
                        }
                    },
                    logger = NoOpLogger()
                )
            )
            .build()

        val result = worker.startWork().get()

        assertEquals(listOf("bootstrap", "sync"), steps)
        assertEquals(ListenableWorker.Result.success(), result)
    }

    private class TestSyncExercisesWorkerFactory(
        private val bootstrapDataSource: ExerciseBootstrapDataSource,
        private val exerciseCatalogSyncer: ExerciseCatalogSyncer,
        private val logger: Logger
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            if (workerClassName != SyncExercisesWorker::class.java.name) {
                return null
            }
            return SyncExercisesWorker(
                appContext = appContext,
                workerParams = workerParameters,
                bootstrapDataSource = bootstrapDataSource,
                exerciseCatalogSyncer = exerciseCatalogSyncer,
                logger = logger
            )
        }
    }

    private class NoOpLogger : Logger {
        override fun log(throwable: Throwable) = Unit
    }
}
