package com.neofit.integration.ai

import com.neofit.domain.coach.CoachService
import com.neofit.domain.model.AppLanguage
import com.neofit.domain.model.CoachContext
import com.neofit.domain.model.DashboardSummary
import com.neofit.domain.model.DietaryPreference
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.UserProfile
import com.neofit.domain.model.WellnessGoal
import com.neofit.domain.model.effectiveDiet
import com.neofit.domain.model.isVegDay
import com.neofit.domain.repository.FoodRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Always-available, fully-offline Coach. It turns the user's real numbers + the
 * bundled [FoodRepository] knowledge base into short, practical, India-aware advice.
 * No model download, no network, no fabricated data. Used as the default engine and
 * as the fallback when an on-device LLM is not (yet) available.
 */
@Singleton
class RuleBasedCoachService @Inject constructor(
    private val foodRepository: FoodRepository,
) : CoachService {

    override val engineLabel: String = "Smart tips"

    override suspend fun isSmartEngineReady(): Boolean = false

    override fun reply(prompt: String, context: CoachContext): Flow<String> = flow {
        val answer = buildAnswer(prompt, context)
        // Pseudo-stream: emit cumulative text word-by-word for a responsive feel.
        val words = answer.split(' ')
        val sb = StringBuilder()
        words.forEachIndexed { i, w ->
            if (i > 0) sb.append(' ')
            sb.append(w)
            emit(sb.toString())
            delay(16)
        }
    }

    private fun buildAnswer(prompt: String, ctx: CoachContext): String {
        val q = prompt.lowercase()
        val d = ctx.dashboard
        val profile = ctx.profile
        val region = profile?.preferredRegion ?: FoodRegion.PAN_INDIA
        val diet = profile?.effectiveDiet() ?: DietaryPreference.VEGETARIAN
        val lang = profile?.language ?: AppLanguage.ENGLISH

        return when {
            q.containsAny("how am i", "today", "status", "doing", "progress", "summary", "score") ->
                statusAnswer(d)

            q.contains("protein") ->
                proteinAnswer(d, region, diet, lang)

            q.containsAny("workout", "exercise", "train", "gym", "yoga", "stretch", "move", "walk") ->
                workoutAnswer(profile)

            q.containsAny("water", "hydrate", "paani", "thirsty", "drink") ->
                "You've had ${d.waterGlasses} of ${d.waterTarget} glasses today. Keep a bottle handy and " +
                    "aim for a glass every couple of hours — easy way to feel sharper and less hungry."

            q.containsAny("veg day", "vegetarian ", "only veg", "no meat") ->
                vegAnswer(ctx, region, lang)

            q.containsAny("eat", "meal", "snack", "dinner", "lunch", "breakfast", "hungry", "food", "recipe", "suggest", "kcal", "calorie") ->
                eatAnswer(q, d, region, diet, lang)

            else -> defaultAnswer(d)
        }
    }

    private fun statusAnswer(d: DashboardSummary): String {
        val remaining = d.caloriesRemaining
        val proteinGap = (d.proteinTargetG - d.proteinConsumedG).roundToInt()
        val parts = StringBuilder()
        parts.append("Today so far: ${d.caloriesConsumed} of ${d.calorieTarget} kcal eaten")
        if (d.caloriesBurned > 0) parts.append(", ${d.caloriesBurned} burned")
        parts.append(". ")
        parts.append(
            if (remaining >= 0) "You have about $remaining kcal left. "
            else "You're about ${-remaining} kcal over — keep dinner light. ",
        )
        parts.append("Protein ${d.proteinConsumedG.roundToInt()}/${d.proteinTargetG} g")
        if (proteinGap > 0) parts.append(" (about $proteinGap g to go)")
        parts.append(". ")
        if (d.steps > 0) parts.append("Steps ${d.steps}/${d.stepTarget}. ")
        parts.append("Wellness ${d.wellness.score}/100.")
        parts.append(
            when {
                d.caloriesConsumed == 0 -> " Log your first meal to get going."
                proteinGap > d.proteinTargetG * 0.3f -> " Add a protein-rich item to your next meal."
                d.steps in 1 until d.stepTarget -> " A short walk will round off your day nicely."
                else -> " You're on track — keep it up!"
            },
        )
        return parts.toString()
    }

    private fun eatAnswer(
        q: String,
        d: DashboardSummary,
        region: FoodRegion,
        diet: DietaryPreference,
        lang: AppLanguage,
    ): String {
        val cap = Regex("(\\d{2,4})").find(q)?.groupValues?.get(1)?.toIntOrNull()
        val budget = (cap ?: d.caloriesRemaining).coerceAtLeast(150)
        val category = categoryFromText(q)
        val picks = foodRepository.recommended(budget, region, diet, category, limit = 5)
            .filter { it.caloriesKcal <= budget + 40 }
            .take(4)
        if (picks.isEmpty()) {
            return "With about $budget kcal to play with, a small bowl of dal with one roti, or a fruit and " +
                "some curd, keeps it light and balanced."
        }
        val list = picks.joinToString(", ") { "${it.displayName(lang)} (~${it.caloriesKcal} kcal)" }
        val capPart = if (cap != null) "Under about $cap kcal" else "With ~$budget kcal left"
        val dietWord = diet.label.lowercase()
        return "$capPart, you could have: $list. These fit a $dietWord plate" +
            (if (category != null) " for ${category.label.lowercase()}" else "") + "."
    }

    private fun proteinAnswer(
        d: DashboardSummary,
        region: FoodRegion,
        diet: DietaryPreference,
        lang: AppLanguage,
    ): String {
        val gap = (d.proteinTargetG - d.proteinConsumedG).roundToInt()
        val picks = foodRepository
            .recommended(d.caloriesRemaining.coerceAtLeast(300), region, diet, null, limit = 25)
            .sortedByDescending { it.proteinG }
            .take(3)
        val list = picks.joinToString(", ") { "${it.displayName(lang)} (~${it.proteinG.roundToInt()} g)" }
        return if (gap <= 0) {
            "Nice — you've already hit your protein target (${d.proteinConsumedG.roundToInt()}/${d.proteinTargetG} g). " +
                "If you want more, ${list.ifEmpty { "dal, paneer or curd" }} are solid picks."
        } else {
            "About $gap g of protein to go. High-protein ${diet.label.lowercase()} picks: " +
                list.ifEmpty { "dal, paneer, eggs or curd" } + ". Try to put one in each meal."
        }
    }

    private fun workoutAnswer(profile: UserProfile?): String {
        val routine = when (profile?.goal) {
            WellnessGoal.WEIGHT_LOSS ->
                "a 25-30 min brisk walk or a 15 min cardio circuit (jumping jacks, high knees, mountain climbers)"
            WellnessGoal.WEIGHT_GAIN ->
                "bodyweight strength: 3 sets of squats, push-ups, lunges and a plank — eat a protein-rich meal after"
            WellnessGoal.IMPROVE_STAMINA ->
                "20 min steady cardio plus 5 rounds of Surya Namaskar to build wind and mobility"
            else ->
                "a balanced 20 min: 10 min cardio + 2 sets of squats, push-ups and a plank"
        }
        return "For your goal, try $routine. Open the Exercise tab for guided plans with demo videos, " +
            "voice cues and a built-in timer."
    }

    private fun vegAnswer(ctx: CoachContext, region: FoodRegion, lang: AppLanguage): String {
        val vegToday = ctx.profile?.isVegDay() == true
        val picks = foodRepository
            .recommended(ctx.dashboard.caloriesRemaining.coerceAtLeast(300), region, DietaryPreference.VEGETARIAN, null, limit = 20)
            .sortedByDescending { it.proteinG }
            .take(4)
        val list = picks.joinToString(", ") { it.displayName(lang) }
        val prefix = if (vegToday) "Today is one of your veg days. " else ""
        return prefix + "Good vegetarian, protein-friendly options: " +
            list.ifEmpty { "dal, rajma, chana, paneer and curd" } + ". Pair a dal/legume with a grain for complete protein."
    }

    private fun defaultAnswer(d: DashboardSummary): String =
        "You have about ${d.caloriesRemaining.coerceAtLeast(0)} kcal left today. I can help with food and workouts — " +
            "ask me things like \"what can I eat for 300 kcal?\", \"veg high-protein dinner\", " +
            "\"how am I doing today?\", or \"plan a quick workout\"."

    private fun categoryFromText(q: String): MealCategory? = when {
        q.contains("breakfast") || q.contains("nashta") -> MealCategory.BREAKFAST
        q.contains("lunch") -> MealCategory.LUNCH
        q.contains("dinner") -> MealCategory.DINNER
        q.contains("snack") -> MealCategory.SNACK
        else -> null
    }

    private fun String.containsAny(vararg needles: String): Boolean = needles.any { this.contains(it) }
}
