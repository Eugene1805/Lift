package com.eugene.lift.domain.usecase.exercise

import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.model.SessionExercise
import com.eugene.lift.domain.model.WorkoutSession
import com.eugene.lift.domain.model.WorkoutSet
import com.eugene.lift.domain.repository.WorkoutRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class GetExerciseProgressionUseCaseTest {

    private lateinit var useCase: GetExerciseProgressionUseCase
    private lateinit var workoutRepository: WorkoutRepository

    private val exerciseId = "ex-1"
    private val exerciseName = "Bench Press"

    @Before
    fun setUp() {
        workoutRepository = mockk(relaxed = true)
        useCase = GetExerciseProgressionUseCase(workoutRepository)
    }

    // ── e1RM Computation ─────────────────────────────────────────────────────

    @Test
    fun `estimatedOneRepMax returns zero for negative or zero weight or reps`() {
        assertEquals(0.0, useCase.estimatedOneRepMax(0.0, 5), 0.001)
        assertEquals(0.0, useCase.estimatedOneRepMax(-10.0, 5), 0.001)
        assertEquals(0.0, useCase.estimatedOneRepMax(100.0, 0), 0.001)
        assertEquals(0.0, useCase.estimatedOneRepMax(100.0, -2), 0.001)
    }

    @Test
    fun `estimatedOneRepMax calculates Epley formula correctly`() {
        // e1RM = 100 * (1 + 5/30) = 116.6667
        assertEquals(116.6667, useCase.estimatedOneRepMax(100.0, 5), 0.001)

        // e1RM = 50 * (1 + 10/30) = 66.6667
        assertEquals(66.6667, useCase.estimatedOneRepMax(50.0, 10), 0.001)
    }

    // ── Build Progression (Weight Based) ────────────────────────────────────

    private fun createSession(
        dateStr: String,
        sets: List<Pair<Double, Int>>,
        completedAll: Boolean = true
    ): WorkoutSession {
        val exercise = Exercise(exerciseId, exerciseName, ExerciseCategory.BARBELL, MeasureType.REPS_AND_WEIGHT, "", null, emptyList())
        val workoutSets = sets.mapIndexed { i, p ->
            WorkoutSet("s$i", weight = p.first, reps = p.second, completed = completedAll)
        }
        val sessionExercise = SessionExercise("se1", exercise, workoutSets)

        return WorkoutSession("session_id", null, "Workout", LocalDateTime.parse(dateStr), 3600, listOf(sessionExercise))
    }

    @Test
    fun `buildProgression for weight based finds best sets and tracks PRs chronologically`() {
        val session1 = createSession("2024-01-01T10:00:00", listOf(100.0 to 1, 100.0 to 2)) // Best e1RM ~106.6
        val session2 = createSession("2024-01-10T10:00:00", listOf(110.0 to 1))              // Best e1RM ~113.6
        val session3 = createSession("2024-01-20T10:00:00", listOf(100.0 to 1))              // Best e1RM ~103.3 (no PR)
        val session4 = createSession("2024-02-01T10:00:00", listOf(100.0 to 10))             // Best e1RM ~133.3 (PR)

        val progression = useCase.buildProgression(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            measureType = MeasureType.REPS_AND_WEIGHT,
            sessions = listOf(session3, session1, session4, session2) // Deliberately out of order
        )

        assertEquals("Should have 4 data points", 4, progression.dataPoints.size)

        // Dates should be sorted chronologically
        assertEquals("2024-01-01", progression.dataPoints[0].date.toString())
        assertEquals("2024-01-10", progression.dataPoints[1].date.toString())
        assertEquals("2024-01-20", progression.dataPoints[2].date.toString())
        assertEquals("2024-02-01", progression.dataPoints[3].date.toString())

        // PR history should be 3 items, newest first
        assertEquals(3, progression.prHistory.size)
        assertEquals("2024-02-01", progression.prHistory[0].date.toString()) // Newest
        assertEquals(10, progression.prHistory[0].reps)
        assertEquals("2024-01-10", progression.prHistory[1].date.toString())
        assertEquals("2024-01-01", progression.prHistory[2].date.toString()) // Oldest
    }

    @Test
    fun `buildProgression ignores uncompleted sets`() {
        val exercise = Exercise(exerciseId, exerciseName, ExerciseCategory.BARBELL, MeasureType.REPS_AND_WEIGHT, "", null, emptyList())

        // A massive set that is NOT completed
        val s1 = WorkoutSet("1", weight = 500.0, reps = 1, completed = false)
        // A small set that IS completed
        val s2 = WorkoutSet("2", weight = 50.0, reps = 5, completed = true)

        val sessionExercise = SessionExercise("se1", exercise, listOf(s1, s2))
        val session = WorkoutSession("sys_id", null, "Workout", LocalDateTime.now(), 3600, listOf(sessionExercise))

        val progression = useCase.buildProgression(exerciseId, exerciseName, MeasureType.REPS_AND_WEIGHT, listOf(session))

        assertEquals(1, progression.dataPoints.size)
        // Best set should be the completed one (50x5), not the uncompleted one (500x1)
        assertEquals(50.0, progression.dataPoints[0].weight, 0.0)
        assertEquals(5, progression.dataPoints[0].reps)
    }

    @Test
    fun `buildProgression ignores sessions without the requested exercise`() {
        val session1 = createSession("2024-01-01T10:00:00", listOf(100.0 to 1))
        
        // Ensure session2 only has a completely different exercise
        val otherExercise = Exercise("other-id", "Squat", ExerciseCategory.BARBELL, MeasureType.REPS_AND_WEIGHT, "", null, emptyList())
        val seOther = SessionExercise("se_other", otherExercise, listOf(WorkoutSet("1", 200.0, 5, completed = true)))
        val session2 = WorkoutSession("sess2", null, "name", LocalDateTime.now(), 3600, listOf(seOther))

        val progression = useCase.buildProgression(exerciseId, exerciseName, MeasureType.REPS_AND_WEIGHT, listOf(session1, session2))

        // Only session1 should be included
        assertEquals(1, progression.dataPoints.size)
    }

    // ── Build Progression (Reps Only) ───────────────────────────────────────

    @Test
    fun `buildProgression for REPS_ONLY uses max reps`() {
        val session1 = createSession("2024-01-01T10:00:00", listOf(0.0 to 10)) // PR: 10
        val session2 = createSession("2024-01-10T10:00:00", listOf(0.0 to 8))  // Not PR
        val session3 = createSession("2024-01-20T10:00:00", listOf(0.0 to 15)) // PR: 15

        val progression = useCase.buildProgression(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            measureType = MeasureType.REPS_ONLY,
            sessions = listOf(session1, session2, session3)
        )

        assertEquals(3, progression.dataPoints.size)
        
        // Check data points y-values (e1RM should be 0, actual reps are used)
        assertTrue(progression.dataPoints.all { it.estimatedOneRepMax == 0.0 })
        assertEquals(10, progression.dataPoints[0].reps)
        assertEquals(8, progression.dataPoints[1].reps)
        assertEquals(15, progression.dataPoints[2].reps)

        // Check PRs
        assertEquals(2, progression.prHistory.size)
        assertEquals(15, progression.prHistory[0].reps) // Newest first
        assertEquals(10, progression.prHistory[1].reps)
    }
}
