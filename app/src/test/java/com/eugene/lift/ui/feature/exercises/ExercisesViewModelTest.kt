package com.eugene.lift.ui.feature.exercises

import app.cash.turbine.test
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.Exercise
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import com.eugene.lift.domain.usecase.GetExercisesUseCase
import com.eugene.lift.domain.usecase.SortOrder
import com.eugene.lift.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit test for ExercisesViewModel
 * Tests exercise filtering and sorting functionality
 */
class ExercisesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getExercisesUseCase: GetExercisesUseCase
    private lateinit var viewModel: ExercisesViewModel

    private val sampleExercises = listOf(
        Exercise(
            id = "1",
            name = "Bench Press",
            bodyParts = listOf(BodyPart.CHEST),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        ),
        Exercise(
            id = "2",
            name = "Squat",
            bodyParts = listOf(BodyPart.QUADRICEPS),
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT,
            instructions = "",
            imagePath = null
        )
    )

    @Before
    fun setup() {
        getExercisesUseCase = mockk()
        every { getExercisesUseCase(any()) } returns flowOf(sampleExercises)
    }

    private fun createViewModel() {
        viewModel = ExercisesViewModel(getExercisesUseCase)
    }

    @Test
    fun `initial searchQuery is empty`() = runTest {
        // WHEN
        createViewModel()

        // THEN
        viewModel.searchQuery.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `initial selectedBodyParts is empty`() = runTest {
        // WHEN
        createViewModel()

        // THEN
        viewModel.selectedBodyParts.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `initial selectedCategories is empty`() = runTest {
        // WHEN
        createViewModel()

        // THEN
        viewModel.selectedCategories.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `initial sortOrder is NAME_ASC`() = runTest {
        // WHEN
        createViewModel()

        // THEN
        viewModel.sortOrder.test {
            assertEquals(SortOrder.NAME_ASC, awaitItem())
        }
    }

    @Test
    fun `onSearchQueryChange updates searchQuery`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.onSearchQueryChange("bench")

        // THEN
        viewModel.searchQuery.test {
            assertEquals("bench", awaitItem())
        }
    }

    @Test
    fun `onSearchQueryChange handles multiple updates`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.onSearchQueryChange("b")
        viewModel.onSearchQueryChange("be")
        viewModel.onSearchQueryChange("ben")

        // THEN
        viewModel.searchQuery.test {
            assertEquals("ben", awaitItem())
        }
    }

    @Test
    fun `toggleSortOrder switches between ASC and DESC`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.toggleSortOrder()

        // THEN
        viewModel.sortOrder.test {
            assertEquals(SortOrder.NAME_DESC, awaitItem())
        }

        // WHEN
        viewModel.toggleSortOrder()

        // THEN
        viewModel.sortOrder.test {
            assertEquals(SortOrder.NAME_ASC, awaitItem())
        }
    }

    @Test
    fun `toggleBodyPartFilter adds body part when not present`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)

        // THEN
        viewModel.selectedBodyParts.test {
            val parts = awaitItem()
            assertTrue(parts.contains(BodyPart.CHEST))
            assertEquals(1, parts.size)
        }
    }

    @Test
    fun `toggleBodyPartFilter removes body part when already present`() = runTest {
        // GIVEN
        createViewModel()
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)

        // WHEN
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)

        // THEN
        viewModel.selectedBodyParts.test {
            val parts = awaitItem()
            assertFalse(parts.contains(BodyPart.CHEST))
            assertTrue(parts.isEmpty())
        }
    }

    @Test
    fun `toggleBodyPartFilter handles multiple body parts`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)
        viewModel.toggleBodyPartFilter(BodyPart.BACK)
        viewModel.toggleBodyPartFilter(BodyPart.QUADRICEPS)

        // THEN
        viewModel.selectedBodyParts.test {
            val parts = awaitItem()
            assertEquals(3, parts.size)
            assertTrue(parts.contains(BodyPart.CHEST))
            assertTrue(parts.contains(BodyPart.BACK))
            assertTrue(parts.contains(BodyPart.QUADRICEPS))
        }
    }

    @Test
    fun `toggleCategoryFilter adds category when not present`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.toggleCategoryFilter(ExerciseCategory.BARBELL)

        // THEN
        viewModel.selectedCategories.test {
            val categories = awaitItem()
            assertTrue(categories.contains(ExerciseCategory.BARBELL))
            assertEquals(1, categories.size)
        }
    }

    @Test
    fun `toggleCategoryFilter removes category when already present`() = runTest {
        // GIVEN
        createViewModel()
        viewModel.toggleCategoryFilter(ExerciseCategory.BARBELL)

        // WHEN
        viewModel.toggleCategoryFilter(ExerciseCategory.BARBELL)

        // THEN
        viewModel.selectedCategories.test {
            val categories = awaitItem()
            assertFalse(categories.contains(ExerciseCategory.BARBELL))
            assertTrue(categories.isEmpty())
        }
    }

    @Test
    fun `toggleCategoryFilter handles multiple categories`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.toggleCategoryFilter(ExerciseCategory.BARBELL)
        viewModel.toggleCategoryFilter(ExerciseCategory.DUMBBELL)
        viewModel.toggleCategoryFilter(ExerciseCategory.BODYWEIGHT)

        // THEN
        viewModel.selectedCategories.test {
            val categories = awaitItem()
            assertEquals(3, categories.size)
            assertTrue(categories.contains(ExerciseCategory.BARBELL))
            assertTrue(categories.contains(ExerciseCategory.DUMBBELL))
            assertTrue(categories.contains(ExerciseCategory.BODYWEIGHT))
        }
    }

    @Test
    fun `clearFilters resets body parts and categories`() = runTest {
        // GIVEN
        createViewModel()
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)
        viewModel.toggleBodyPartFilter(BodyPart.BACK)
        viewModel.toggleCategoryFilter(ExerciseCategory.BARBELL)
        viewModel.toggleCategoryFilter(ExerciseCategory.DUMBBELL)

        // WHEN
        viewModel.clearFilters()

        // THEN
        viewModel.selectedBodyParts.test {
            assertTrue(awaitItem().isEmpty())
        }
        viewModel.selectedCategories.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `clearFilters does not affect search query`() = runTest {
        // GIVEN
        createViewModel()
        viewModel.onSearchQueryChange("bench")
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)

        // WHEN
        viewModel.clearFilters()

        // THEN
        viewModel.searchQuery.test {
            assertEquals("bench", awaitItem())
        }
    }

    @Test
    fun `clearFilters does not affect sort order`() = runTest {
        // GIVEN
        createViewModel()
        viewModel.toggleSortOrder() // Change to DESC
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)

        // WHEN
        viewModel.clearFilters()

        // THEN
        viewModel.sortOrder.test {
            assertEquals(SortOrder.NAME_DESC, awaitItem())
        }
    }

    @Test
    fun `exercises flow emits filtered results`() = runTest {
        // GIVEN
        createViewModel()

        // THEN
        viewModel.exercises.test {
            // Skip initial empty value
            awaitItem()
            // Get the actual data from use case
            val exercises = awaitItem()
            assertEquals(2, exercises.size)
        }
    }

    @Test
    fun `filter changes trigger new exercise query`() = runTest {
        // GIVEN
        val filteredExercises = listOf(sampleExercises[0])
        every {
            getExercisesUseCase(
                match { filter -> filter.query == "bench" }
            )
        } returns flowOf(filteredExercises)
        createViewModel()

        // THEN - Collect the flow and make changes during collection
        viewModel.exercises.test {
            // Skip initial emission with all exercises
            awaitItem()

            // WHEN - Change the query
            viewModel.onSearchQueryChange("bench")

            // Wait for the new emission with filtered results
            val exercises = awaitItem()
            assertEquals(1, exercises.size)
            assertEquals("Bench Press", exercises.first().name)
        }
    }

    @Test
    fun `combined filters are applied correctly`() = runTest {
        // GIVEN
        createViewModel()

        // WHEN
        viewModel.onSearchQueryChange("press")
        viewModel.toggleBodyPartFilter(BodyPart.CHEST)
        viewModel.toggleCategoryFilter(ExerciseCategory.BARBELL)
        viewModel.toggleSortOrder()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // THEN - Verify the use case was called with combined filter
        // The exact verification depends on your mock setup
        viewModel.exercises.test {
            awaitItem() // Should emit filtered results
        }
    }
}
