package com.neofit

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.neofit.data.local.NeoFitDatabase
import com.neofit.data.local.entity.MealLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** On-device validation of the Room schema and meal-log persistence. */
@RunWith(AndroidJUnit4::class)
class NeoFitDatabaseTest {

    private lateinit var db: NeoFitDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, NeoFitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndReadMeal_persistsAcrossDao() = runBlocking {
        val dao = db.mealLogDao()
        val id = dao.insert(
            MealLogEntity(
                foodId = "idli",
                name = "Idli",
                category = "BREAKFAST",
                region = "SOUTH",
                portionLabel = "2 pieces",
                portionMultiplier = 2f,
                portionGrams = 100,
                caloriesKcal = 140,
                proteinG = 5f,
                carbsG = 30f,
                fatG = 1f,
                fiberG = 2f,
                confidence = "HIGH",
                basis = "test",
                isApproximate = false,
                timestampEpochMillis = 1_000L,
                epochDay = 0L,
                imageRef = null,
                manuallyCorrected = false,
                source = "SEARCH",
                note = null,
            ),
        )

        val byId = dao.getById(id)
        assertThat(byId).isNotNull()
        assertThat(byId!!.name).isEqualTo("Idli")

        val forDay = dao.observeForDay(0L).first()
        assertThat(forDay).hasSize(1)
        assertThat(forDay.first().caloriesKcal).isEqualTo(140)
    }
}
