package com.eugene.lift.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eugene.lift.data.local.AppDatabase
import com.eugene.lift.data.local.dao.ExerciseDao
import com.eugene.lift.data.local.entity.ExerciseEntity
import com.eugene.lift.domain.model.BodyPart
import com.eugene.lift.domain.model.ExerciseCategory
import com.eugene.lift.domain.model.MeasureType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // InMemoryDatabaseBuilder crea una DB en RAM.
        // Se borra al terminar el test. Perfecto para pruebas.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.exerciseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUserAndReadInList() = runTest {
        // 1. GIVEN: Un ejercicio con Enums específicos
        val exercise = ExerciseEntity(
            name = "Press Militar",
            bodyPart = BodyPart.SHOULDERS,
            category = ExerciseCategory.BARBELL,
            measureType = MeasureType.REPS_AND_WEIGHT
        )

        // 2. WHEN: Lo insertamos
        dao.insertExercise(exercise)

        // 3. THEN: Lo buscamos y verificamos que los Enums volvieron intactos
        // .first() toma el primer valor del Flow y cancela la suscripción
        val byId = dao.getExerciseById(exercise.id)

        assertEquals("Press Militar", byId?.name)
        assertEquals(BodyPart.SHOULDERS, byId?.bodyPart)     // Testea el Converter de ida y vuelta
        assertEquals(ExerciseCategory.BARBELL, byId?.category)
        assertEquals(MeasureType.REPS_AND_WEIGHT, byId?.measureType)
    }
}