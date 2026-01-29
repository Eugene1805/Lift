package com.eugene.lift.domain.usecase.settings

import android.util.Log
import com.eugene.lift.domain.model.WeightUnit
import com.eugene.lift.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit test for UpdateWeightUnitUseCase
 * Tests weight unit update functionality
 */
class UpdateWeightUnitUseCaseTest {

    private lateinit var repository: SettingsRepository
    private lateinit var useCase: UpdateWeightUnitUseCase

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        repository = mockk(relaxed = true)
        useCase = UpdateWeightUnitUseCase(repository)
    }

    @Test
    fun `invoke updates weight unit to KG`() = runTest {
        // WHEN
        useCase(WeightUnit.KG)

        // THEN
        coVerify(exactly = 1) { repository.setWeightUnit(WeightUnit.KG) }
    }

    @Test
    fun `invoke updates weight unit to LBS`() = runTest {
        // WHEN
        useCase(WeightUnit.LBS)

        // THEN
        coVerify(exactly = 1) { repository.setWeightUnit(WeightUnit.LBS) }
    }

    @Test
    fun `invoke handles multiple weight unit updates`() = runTest {
        // WHEN
        useCase(WeightUnit.KG)
        useCase(WeightUnit.LBS)
        useCase(WeightUnit.KG)

        // THEN
        coVerify(exactly = 2) { repository.setWeightUnit(WeightUnit.KG) }
        coVerify(exactly = 1) { repository.setWeightUnit(WeightUnit.LBS) }
    }

    @Test
    fun `invoke can toggle between weight units`() = runTest {
        // WHEN
        useCase(WeightUnit.KG)
        useCase(WeightUnit.LBS)
        useCase(WeightUnit.KG)
        useCase(WeightUnit.LBS)

        // THEN
        coVerify(exactly = 2) { repository.setWeightUnit(WeightUnit.KG) }
        coVerify(exactly = 2) { repository.setWeightUnit(WeightUnit.LBS) }
    }
}
