package com.eugene.lift.domain.usecase.settings

import android.util.Log
import com.eugene.lift.domain.model.DistanceUnit
import com.eugene.lift.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for UpdateDistanceUnitUseCase
 * Tests distance unit update functionality
 */
class UpdateDistanceUnitUseCaseTest {

    private lateinit var repository: SettingsRepository
    private lateinit var useCase: UpdateDistanceUnitUseCase

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        repository = mockk(relaxed = true)
        useCase = UpdateDistanceUnitUseCase(repository)
    }

    @Test
    fun `invoke updates distance unit to KM`() = runTest {
        // WHEN
        useCase(DistanceUnit.KM)

        // THEN
        coVerify(exactly = 1) { repository.setDistanceUnit(DistanceUnit.KM) }
    }

    @Test
    fun `invoke updates distance unit to MILES`() = runTest {
        // WHEN
        useCase(DistanceUnit.MILES)

        // THEN
        coVerify(exactly = 1) { repository.setDistanceUnit(DistanceUnit.MILES) }
    }

    @Test
    fun `invoke handles multiple distance unit updates`() = runTest {
        // WHEN
        useCase(DistanceUnit.KM)
        useCase(DistanceUnit.MILES)
        useCase(DistanceUnit.KM)

        // THEN
        coVerify(exactly = 2) { repository.setDistanceUnit(DistanceUnit.KM) }
        coVerify(exactly = 1) { repository.setDistanceUnit(DistanceUnit.MILES) }
    }

    @Test
    fun `invoke can toggle between distance units`() = runTest {
        // WHEN
        useCase(DistanceUnit.KM)
        useCase(DistanceUnit.MILES)
        useCase(DistanceUnit.KM)
        useCase(DistanceUnit.MILES)

        // THEN
        coVerify(exactly = 2) { repository.setDistanceUnit(DistanceUnit.KM) }
        coVerify(exactly = 2) { repository.setDistanceUnit(DistanceUnit.MILES) }
    }
}
