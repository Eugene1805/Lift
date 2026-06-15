package com.eugene.lift.data.local

import android.content.Context
import com.eugene.lift.R
import com.eugene.lift.domain.model.Exercise
import java.text.Normalizer

object SeedExerciseStrings {

    @Volatile
    private var cachedLocaleTag: String? = null

    @Volatile
    private var cachedNameToSeedKey: Map<String, String> = emptyMap()

    fun attachSeedKeys(context: Context, exercises: List<Exercise>): List<Exercise> {
        return exercises.map { exercise ->
            exercise.copy(seedKey = findSeedKeyByName(context, exercise.name))
        }
    }

    fun findSeedKeyByName(context: Context, exerciseName: String): String? {
        ensureNameCache(context)
        return cachedNameToSeedKey[normalize(exerciseName)]
    }

    fun localize(context: Context, exercise: Exercise): Exercise {
        val seedKey = exercise.seedKey ?: return exercise
        return exercise.copy(
            name = resolveString(context, seedKey) ?: exercise.name,
            instructions = resolveString(context, "${seedKey}_desc") ?: exercise.instructions
        )
    }

    private fun ensureNameCache(context: Context) {
        val localeTag = context.resources.configuration.locales[0]?.toLanguageTag().orEmpty()
        if (cachedLocaleTag == localeTag) return

        synchronized(this) {
            if (cachedLocaleTag == localeTag) return
            cachedNameToSeedKey = buildNameToSeedKeyMap(context)
            cachedLocaleTag = localeTag
        }
    }

    private fun buildNameToSeedKeyMap(context: Context): Map<String, String> {
        val nameToSeedKey = linkedMapOf<String, String>()
        for (field in R.string::class.java.fields) {
            if (!field.name.startsWith("seed_") || field.name.endsWith("_desc")) {
                continue
            }
            val resId = field.getInt(null)
            val resolvedName = context.getString(resId)
            nameToSeedKey[normalize(resolvedName)] = field.name
        }
        return nameToSeedKey
    }

    private fun resolveString(context: Context, entryName: String): String? {
        val resId = context.resources.getIdentifier(entryName, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value.trim().lowercase(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
    }
}
