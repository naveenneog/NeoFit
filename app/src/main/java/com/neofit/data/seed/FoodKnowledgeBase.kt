package com.neofit.data.seed

import com.neofit.domain.model.ConfidenceLevel
import com.neofit.domain.model.ConfidenceLevel.HIGH
import com.neofit.domain.model.ConfidenceLevel.MEDIUM
import com.neofit.domain.model.ConfidenceLevel.ROUGH
import com.neofit.domain.model.CookingStyle
import com.neofit.domain.model.CookingStyle.BOILED
import com.neofit.domain.model.CookingStyle.CURRY
import com.neofit.domain.model.CookingStyle.FRIED
import com.neofit.domain.model.CookingStyle.RAW
import com.neofit.domain.model.CookingStyle.ROASTED
import com.neofit.domain.model.CookingStyle.STEAMED
import com.neofit.domain.model.FoodDiet
import com.neofit.domain.model.FoodDiet.EGG
import com.neofit.domain.model.FoodDiet.NONVEG
import com.neofit.domain.model.FoodDiet.VEG
import com.neofit.domain.model.FoodDiet.VEGAN
import com.neofit.domain.model.FoodItem
import com.neofit.domain.model.FoodRegion
import com.neofit.domain.model.FoodRegion.CENTRAL
import com.neofit.domain.model.FoodRegion.EAST
import com.neofit.domain.model.FoodRegion.NORTH
import com.neofit.domain.model.FoodRegion.NORTH_EAST
import com.neofit.domain.model.FoodRegion.PAN_INDIA
import com.neofit.domain.model.FoodRegion.SOUTH
import com.neofit.domain.model.FoodRegion.WEST
import com.neofit.domain.model.MealCategory
import com.neofit.domain.model.MealCategory.BREAKFAST
import com.neofit.domain.model.MealCategory.DINNER
import com.neofit.domain.model.MealCategory.LUNCH
import com.neofit.domain.model.MealCategory.SNACK
import com.neofit.domain.model.PortionSize

/**
 * In-memory Indian food knowledge base used as the calorie lookup table.
 *
 * Nutrition is per the listed [PortionSize] and is intentionally approximate —
 * home and street recipes vary widely. `baseConfidence` reflects how stable a
 * dish's values are (steamed staples = HIGH, combos/street food = ROUGH).
 *
 * `localizedNames` carries authentic native dish names (never invented words);
 * romanized blends (Hinglish/Kanglish) reuse the already-native `nameEn`.
 */
object FoodKnowledgeBase {

    private fun item(
        id: String,
        name: String,
        region: FoodRegion,
        diet: FoodDiet,
        category: MealCategory,
        serving: PortionSize,
        kcal: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        fiber: Float = 0f,
        style: CookingStyle = CURRY,
        street: Boolean = false,
        sweet: Boolean = false,
        confidence: ConfidenceLevel = MEDIUM,
        ingredients: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        aliases: List<String> = emptyList(),
        names: Map<String, String> = emptyMap(),
    ) = FoodItem(
        id = id,
        nameEn = name,
        localizedNames = names,
        region = region,
        diet = diet,
        typicalCategory = category,
        baseServing = serving,
        caloriesKcal = kcal,
        proteinG = protein,
        carbsG = carbs,
        fatG = fat,
        fiberG = fiber,
        cookingStyle = style,
        ingredients = ingredients,
        tags = tags,
        aliases = aliases,
        isStreetFood = street,
        isSweet = sweet,
        baseConfidence = confidence,
    )

    private val p2 = PortionSize("2 pieces", 2f)
    private val plate = PortionSize.PLATE
    private val katori = PortionSize.KATORI
    private val glass = PortionSize.GLASS
    private val piece = PortionSize.PIECE

    val foods: List<FoodItem> = listOf(
        // ---------------- SOUTH INDIA ----------------
        item("idli", "Idli", SOUTH, VEGAN, BREAKFAST, p2, 140, 5f, 30f, 1f, 2f, STEAMED,
            confidence = HIGH, ingredients = listOf("rice", "urad dal"), tags = listOf("homemade"),
            aliases = listOf("idly"),
            names = mapOf("hi" to "इडली", "kn" to "ಇಡ್ಲಿ", "ta" to "இட்லி", "te" to "ఇడ్లీ")),
        item("dosa", "Dosa", SOUTH, VEGAN, BREAKFAST, piece, 170, 4f, 30f, 4f, 1f, ROASTED,
            confidence = HIGH, ingredients = listOf("rice", "urad dal"), tags = listOf("homemade"),
            aliases = listOf("dose", "thosai"),
            names = mapOf("hi" to "डोसा", "kn" to "ದೋಸೆ", "ta" to "தோசை", "te" to "దోస")),
        item("masala_dosa", "Masala Dosa", SOUTH, VEG, BREAKFAST, piece, 290, 6f, 45f, 9f, 3f, ROASTED,
            ingredients = listOf("rice", "urad dal", "potato"), tags = listOf("combo"),
            names = mapOf("hi" to "मसाला डोसा", "kn" to "ಮಸಾಲ ದೋಸೆ", "ta" to "மசாலா தோசை")),
        item("sambar", "Sambar", SOUTH, VEGAN, LUNCH, katori, 120, 5f, 18f, 3f, 4f, CURRY,
            confidence = HIGH, ingredients = listOf("toor dal", "tamarind", "vegetables"),
            names = mapOf("hi" to "सांभर", "kn" to "ಸಾಂಬಾರ್", "ta" to "சாம்பார்", "te" to "సాంబార్")),
        item("pongal", "Ven Pongal", SOUTH, VEG, BREAKFAST, katori, 260, 7f, 40f, 8f, 2f, BOILED,
            ingredients = listOf("rice", "moong dal", "ghee", "pepper"),
            names = mapOf("ta" to "பொங்கல்", "te" to "పొంగల్")),
        item("upma", "Upma", SOUTH, VEG, BREAKFAST, katori, 250, 6f, 38f, 8f, 2f, ROASTED,
            ingredients = listOf("rava", "vegetables"),
            names = mapOf("hi" to "उपमा", "kn" to "ಉಪ್ಪಿಟ್ಟು", "ta" to "உப்புமா")),
        item("medu_vada", "Medu Vada", SOUTH, VEG, SNACK, p2, 280, 7f, 30f, 14f, 2f, FRIED,
            street = true, confidence = ROUGH, ingredients = listOf("urad dal"),
            tags = listOf("street", "snack"), aliases = listOf("vada", "ulundu vadai")),
        item("curd_rice", "Curd Rice", SOUTH, VEG, LUNCH, katori, 200, 5f, 32f, 5f, 1f, BOILED,
            confidence = HIGH, ingredients = listOf("rice", "curd"),
            aliases = listOf("thayir sadam", "mosaranna"),
            names = mapOf("ta" to "தயிர் சாதம்", "kn" to "ಮೊಸರನ್ನ")),
        item("filter_coffee", "Filter Coffee", SOUTH, VEG, BREAKFAST, PortionSize("1 cup", 1f, 120),
            90, 3f, 12f, 3f, 0f, RAW, tags = listOf("beverage"),
            names = mapOf("ta" to "காபி", "kn" to "ಕಾಫಿ")),
        item("murukku", "Murukku", SOUTH, VEGAN, SNACK, PortionSize("1 handful", 1f, 30), 150, 2f, 15f, 9f, 1f,
            FRIED, confidence = ROUGH, tags = listOf("snack"), aliases = listOf("chakli")),
        item("fish_curry_south", "Fish Curry (South)", SOUTH, NONVEG, LUNCH, katori, 220, 20f, 6f, 12f, 1f, CURRY,
            ingredients = listOf("fish", "coconut", "tamarind"), tags = listOf("coastal"),
            aliases = listOf("meen kuzhambu")),

        // ---------------- NORTH INDIA ----------------
        item("aloo_paratha", "Aloo Paratha", NORTH, VEG, BREAKFAST, piece, 290, 6f, 40f, 11f, 4f, ROASTED,
            ingredients = listOf("wheat", "potato", "ghee"),
            names = mapOf("hi" to "आलू पराठा")),
        item("paratha", "Plain Paratha", NORTH, VEG, BREAKFAST, piece, 260, 5f, 36f, 10f, 2f, ROASTED,
            names = mapOf("hi" to "पराठा")),
        item("roti", "Roti", PAN_INDIA, VEGAN, LUNCH, piece, 100, 3f, 18f, 2f, 3f, ROASTED,
            confidence = HIGH, ingredients = listOf("wheat"), aliases = listOf("chapati", "phulka"),
            names = mapOf("hi" to "रोटी", "ta" to "சப்பாத்தி")),
        item("dal_tadka", "Dal Tadka", NORTH, VEG, LUNCH, katori, 180, 9f, 22f, 6f, 5f, CURRY,
            confidence = HIGH, ingredients = listOf("toor dal", "ghee", "spices"),
            names = mapOf("hi" to "दाल तड़का")),
        item("dal_chawal", "Dal Chawal", PAN_INDIA, VEG, LUNCH, plate, 400, 14f, 65f, 8f, 6f, BOILED,
            ingredients = listOf("dal", "rice"), tags = listOf("combo", "homemade"),
            aliases = listOf("dal rice"), names = mapOf("hi" to "दाल चावल")),
        item("rajma_chawal", "Rajma Chawal", NORTH, VEG, LUNCH, plate, 480, 16f, 78f, 9f, 9f, CURRY,
            ingredients = listOf("kidney beans", "rice"), tags = listOf("combo"),
            names = mapOf("hi" to "राजमा चावल")),
        item("chole_bhature", "Chole Bhature", NORTH, VEG, LUNCH, plate, 650, 16f, 80f, 28f, 10f, FRIED,
            street = true, confidence = ROUGH, ingredients = listOf("chickpeas", "maida"),
            tags = listOf("combo", "street"), names = mapOf("hi" to "छोले भटूरे")),
        item("paneer_butter_masala", "Paneer Butter Masala", NORTH, VEG, DINNER, katori, 320, 12f, 12f, 24f, 2f, CURRY,
            ingredients = listOf("paneer", "tomato", "cream", "butter"),
            names = mapOf("hi" to "पनीर बटर मसाला")),
        item("palak_paneer", "Palak Paneer", NORTH, VEG, DINNER, katori, 240, 12f, 10f, 16f, 4f, CURRY,
            ingredients = listOf("spinach", "paneer"), names = mapOf("hi" to "पालक पनीर")),
        item("paneer_tikka", "Paneer Tikka", NORTH, VEG, SNACK, PortionSize("100 g", 1f, 100), 270, 16f, 8f, 18f, 1f,
            CookingStyle.GRILLED, ingredients = listOf("paneer", "spices"), tags = listOf("snack")),
        item("chicken_curry", "Chicken Curry", PAN_INDIA, NONVEG, DINNER, katori, 240, 22f, 6f, 14f, 1f, CURRY,
            ingredients = listOf("chicken", "onion", "tomato", "spices"),
            names = mapOf("hi" to "चिकन करी")),
        item("butter_chicken", "Butter Chicken", NORTH, NONVEG, DINNER, katori, 340, 22f, 10f, 22f, 1f, CURRY,
            confidence = MEDIUM, ingredients = listOf("chicken", "butter", "cream", "tomato"),
            aliases = listOf("murgh makhani"), names = mapOf("hi" to "बटर चिकन")),
        item("egg_bhurji", "Egg Bhurji", PAN_INDIA, EGG, BREAKFAST, katori, 220, 14f, 4f, 16f, 1f, FRIED,
            ingredients = listOf("eggs", "onion", "spices"), names = mapOf("hi" to "एग भुर्जी")),
        item("boiled_egg", "Boiled Egg", PAN_INDIA, EGG, SNACK, piece, 78, 6f, 1f, 5f, 0f, BOILED,
            confidence = HIGH, aliases = listOf("anda")),
        item("omelette", "Omelette", PAN_INDIA, EGG, BREAKFAST, PortionSize("2-egg", 1f), 240, 14f, 3f, 18f, 0f, FRIED,
            ingredients = listOf("eggs", "onion")),
        item("jeera_rice", "Jeera Rice", PAN_INDIA, VEG, LUNCH, katori, 200, 4f, 38f, 4f, 1f, BOILED,
            ingredients = listOf("rice", "cumin", "ghee")),
        item("veg_pulao", "Veg Pulao", PAN_INDIA, VEG, LUNCH, katori, 250, 6f, 42f, 6f, 3f, BOILED,
            ingredients = listOf("rice", "vegetables"), aliases = listOf("pulav"),
            names = mapOf("hi" to "पुलाव")),
        item("chicken_biryani", "Chicken Biryani", PAN_INDIA, NONVEG, LUNCH, plate, 600, 25f, 75f, 22f, 3f, CURRY,
            confidence = ROUGH, ingredients = listOf("rice", "chicken", "spices"),
            tags = listOf("combo"), names = mapOf("hi" to "चिकन बिरयानी", "ta" to "பிரியாணி")),
        item("veg_biryani", "Veg Biryani", PAN_INDIA, VEG, LUNCH, plate, 480, 11f, 78f, 14f, 5f, CURRY,
            confidence = ROUGH, tags = listOf("combo"), names = mapOf("hi" to "वेज बिरयानी")),
        item("naan", "Naan", NORTH, VEG, DINNER, piece, 260, 7f, 45f, 6f, 2f, CookingStyle.BAKED,
            ingredients = listOf("maida", "yogurt")),
        item("masala_chai", "Masala Chai", PAN_INDIA, VEG, BREAKFAST, PortionSize("1 cup", 1f, 120), 90, 2f, 12f, 3f, 0f,
            RAW, confidence = HIGH, tags = listOf("beverage"), aliases = listOf("chai", "tea"),
            names = mapOf("hi" to "चाय")),
        item("sweet_lassi", "Sweet Lassi", NORTH, VEG, SNACK, glass, 220, 6f, 32f, 7f, 0f, RAW,
            tags = listOf("beverage", "sweet"), names = mapOf("hi" to "लस्सी")),
        item("buttermilk", "Buttermilk", PAN_INDIA, VEG, SNACK, glass, 60, 3f, 5f, 3f, 0f, RAW,
            confidence = HIGH, tags = listOf("beverage"), aliases = listOf("chaas", "majjige", "mor"),
            names = mapOf("hi" to "छाछ", "kn" to "ಮಜ್ಜಿಗೆ")),
        item("samosa", "Samosa", PAN_INDIA, VEGAN, SNACK, piece, 150, 3f, 18f, 8f, 2f, FRIED,
            street = true, confidence = ROUGH, ingredients = listOf("maida", "potato", "peas"),
            tags = listOf("street", "snack"), names = mapOf("hi" to "समोसा")),
        item("pakoda", "Pakoda", PAN_INDIA, VEGAN, SNACK, plate, 300, 8f, 30f, 16f, 4f, FRIED,
            street = true, confidence = ROUGH, ingredients = listOf("besan", "vegetables"),
            tags = listOf("street", "snack"), aliases = listOf("bhajji", "bajji", "pakora")),
        item("gulab_jamun", "Gulab Jamun", PAN_INDIA, VEG, SNACK, p2, 300, 4f, 50f, 10f, 0f, FRIED,
            sweet = true, confidence = MEDIUM, tags = listOf("sweet", "festival"),
            names = mapOf("hi" to "गुलाब जामुन")),
        item("jalebi", "Jalebi", PAN_INDIA, VEG, SNACK, PortionSize("100 g", 1f, 100), 350, 2f, 60f, 12f, 0f, FRIED,
            sweet = true, street = true, confidence = ROUGH, tags = listOf("sweet", "street"),
            names = mapOf("hi" to "जलेबी")),
        item("besan_ladoo", "Besan Ladoo", PAN_INDIA, VEG, SNACK, piece, 180, 4f, 22f, 9f, 1f, ROASTED,
            sweet = true, tags = listOf("sweet", "festival"), aliases = listOf("laddu"),
            names = mapOf("hi" to "बेसन लड्डू")),
        item("chikki", "Chikki", PAN_INDIA, VEGAN, SNACK, PortionSize("1 piece", 1f, 30), 150, 4f, 16f, 8f, 1f, ROASTED,
            sweet = true, tags = listOf("sweet", "snack"), aliases = listOf("gajak")),

        // ---------------- WEST INDIA ----------------
        item("poha", "Poha", CENTRAL, VEGAN, BREAKFAST, katori, 250, 5f, 40f, 7f, 2f, ROASTED,
            confidence = HIGH, ingredients = listOf("flattened rice", "onion", "peanuts"),
            tags = listOf("homemade"), names = mapOf("hi" to "पोहा")),
        item("vada_pav", "Vada Pav", WEST, VEGAN, SNACK, piece, 290, 7f, 42f, 11f, 3f, FRIED,
            street = true, confidence = ROUGH, ingredients = listOf("potato", "pav", "besan"),
            tags = listOf("street"), names = mapOf("hi" to "वडा पाव")),
        item("misal_pav", "Misal Pav", WEST, VEGAN, BREAKFAST, plate, 400, 14f, 55f, 14f, 9f, CURRY,
            street = true, confidence = ROUGH, ingredients = listOf("sprouts", "pav"),
            tags = listOf("street", "combo")),
        item("pav_bhaji", "Pav Bhaji", WEST, VEG, DINNER, plate, 400, 9f, 55f, 16f, 7f, ROASTED,
            street = true, confidence = ROUGH, ingredients = listOf("vegetables", "butter", "pav"),
            tags = listOf("street", "combo"), names = mapOf("hi" to "पाव भाजी")),
        item("dhokla", "Dhokla", WEST, VEG, SNACK, PortionSize("3 pieces", 1.5f), 160, 6f, 24f, 4f, 2f, STEAMED,
            confidence = HIGH, ingredients = listOf("besan", "fermented"), tags = listOf("snack"),
            names = mapOf("hi" to "ढोकला")),
        item("thepla", "Thepla", WEST, VEGAN, BREAKFAST, p2, 220, 6f, 32f, 8f, 3f, ROASTED,
            ingredients = listOf("wheat", "fenugreek")),
        item("sev", "Sev", WEST, VEGAN, SNACK, PortionSize("30 g", 1f, 30), 160, 4f, 14f, 10f, 1f, FRIED,
            confidence = MEDIUM, tags = listOf("snack")),
        item("shrikhand", "Shrikhand", WEST, VEG, SNACK, katori, 250, 7f, 36f, 9f, 0f, RAW,
            sweet = true, tags = listOf("sweet")),

        // ---------------- EAST INDIA ----------------
        item("macher_jhol", "Macher Jhol", EAST, NONVEG, LUNCH, katori, 200, 20f, 8f, 10f, 1f, CURRY,
            ingredients = listOf("fish", "potato", "mustard"), aliases = listOf("fish curry bengali"),
            names = mapOf("bn" to "মাছের ঝোল")),
        item("aloo_posto", "Aloo Posto", EAST, VEGAN, LUNCH, katori, 200, 4f, 24f, 10f, 3f, CURRY,
            ingredients = listOf("potato", "poppy seeds"), names = mapOf("bn" to "আলু পোস্ত")),
        item("rasgulla", "Rasgulla", EAST, VEG, SNACK, p2, 250, 6f, 45f, 5f, 0f, BOILED,
            sweet = true, confidence = MEDIUM, tags = listOf("sweet", "festival"),
            names = mapOf("bn" to "রসগোল্লা", "hi" to "रसगुल्ला")),
        item("mishti_doi", "Mishti Doi", EAST, VEG, SNACK, katori, 200, 6f, 32f, 5f, 0f, RAW,
            sweet = true, tags = listOf("sweet"), names = mapOf("bn" to "মিষ্টি দই")),

        // ---------------- CENTRAL INDIA ----------------
        item("litti_chokha", "Litti Chokha", CENTRAL, VEG, LUNCH, p2, 380, 10f, 52f, 14f, 7f, ROASTED,
            confidence = MEDIUM, ingredients = listOf("wheat", "sattu", "brinjal", "potato"),
            tags = listOf("combo"), names = mapOf("hi" to "लिट्टी चोखा")),
        item("dal_bati", "Dal Bati", CENTRAL, VEG, LUNCH, plate, 550, 15f, 70f, 22f, 8f, CookingStyle.BAKED,
            confidence = ROUGH, ingredients = listOf("wheat", "dal", "ghee"), tags = listOf("combo"),
            aliases = listOf("dal bati churma"), names = mapOf("hi" to "दाल बाटी")),
        item("bhutte_ka_kees", "Bhutte Ka Kees", CENTRAL, VEG, SNACK, katori, 200, 6f, 28f, 8f, 3f, ROASTED,
            ingredients = listOf("corn", "milk"), tags = listOf("snack")),

        // ---------------- NORTH EAST INDIA ----------------
        item("veg_momos", "Veg Momos", NORTH_EAST, VEGAN, SNACK, PortionSize("5 pieces", 1f), 250, 7f, 42f, 6f, 3f,
            STEAMED, confidence = MEDIUM, ingredients = listOf("maida", "vegetables"),
            tags = listOf("street", "snack"), names = mapOf("hi" to "मोमोज")),
        item("chicken_momos", "Chicken Momos", NORTH_EAST, NONVEG, SNACK, PortionSize("5 pieces", 1f), 300, 14f, 40f, 9f,
            2f, STEAMED, confidence = MEDIUM, ingredients = listOf("maida", "chicken"),
            tags = listOf("street", "snack")),
        item("thukpa", "Thukpa", NORTH_EAST, NONVEG, DINNER, PortionSize("1 bowl", 1.5f, 350), 350, 18f, 48f, 9f, 4f,
            BOILED, ingredients = listOf("noodles", "vegetables", "chicken"), tags = listOf("soup")),
        item("bamboo_shoot_curry", "Bamboo Shoot Curry", NORTH_EAST, VEGAN, LUNCH, katori, 150, 4f, 16f, 7f, 4f, CURRY,
            confidence = ROUGH, ingredients = listOf("bamboo shoot", "spices")),
        item("pork_bamboo_shoot", "Pork with Bamboo Shoot", NORTH_EAST, NONVEG, LUNCH, katori, 320, 20f, 8f, 22f, 2f,
            CURRY, confidence = ROUGH, ingredients = listOf("pork", "bamboo shoot")),

        // ---------------- PAN-INDIA / HOMEMADE / STAPLES ----------------
        item("khichdi", "Khichdi", PAN_INDIA, VEG, DINNER, katori, 250, 9f, 40f, 5f, 4f, BOILED,
            confidence = HIGH, ingredients = listOf("rice", "moong dal"), tags = listOf("homemade"),
            aliases = listOf("khichuri"), names = mapOf("hi" to "खिचड़ी")),
        item("roti_sabzi", "Roti Sabzi", PAN_INDIA, VEG, LUNCH, plate, 350, 10f, 50f, 12f, 8f, CURRY,
            ingredients = listOf("roti", "mixed vegetables"), tags = listOf("combo", "homemade"),
            names = mapOf("hi" to "रोटी सब्ज़ी")),
        item("mixed_veg_sabzi", "Mixed Veg Sabzi", PAN_INDIA, VEGAN, LUNCH, katori, 150, 4f, 16f, 8f, 5f, CURRY,
            ingredients = listOf("mixed vegetables"), aliases = listOf("sabji")),
        item("plain_rice", "Plain Rice", PAN_INDIA, VEGAN, LUNCH, katori, 200, 4f, 44f, 1f, 1f, BOILED,
            confidence = HIGH, aliases = listOf("chawal", "sadam", "anna"),
            names = mapOf("hi" to "चावल")),
        item("curd", "Curd", PAN_INDIA, VEG, SNACK, katori, 100, 5f, 7f, 5f, 0f, RAW,
            confidence = HIGH, aliases = listOf("dahi", "mosaru", "thayir"),
            names = mapOf("hi" to "दही")),
        item("milk", "Milk", PAN_INDIA, VEG, BREAKFAST, glass, 150, 8f, 12f, 8f, 0f, RAW,
            confidence = HIGH, tags = listOf("beverage"), aliases = listOf("doodh")),
        item("banana", "Banana", PAN_INDIA, VEGAN, SNACK, piece, 105, 1f, 27f, 0f, 3f, RAW,
            confidence = HIGH, tags = listOf("fruit"), aliases = listOf("kela")),
        item("green_tea", "Green Tea", PAN_INDIA, VEGAN, SNACK, PortionSize("1 cup", 1f, 120), 2, 0f, 0f, 0f, 0f, RAW,
            confidence = HIGH, tags = listOf("beverage")),

        // ---------------- MILLETS & DECCAN STAPLES ----------------
        item("ragi_mudde", "Ragi Mudde", SOUTH, VEGAN, LUNCH, piece, 130, 3f, 27f, 1f, 4f, BOILED,
            confidence = HIGH, ingredients = listOf("finger millet", "ragi flour"),
            tags = listOf("homemade", "millet"),
            aliases = listOf("ragi ball", "ragi sankati", "ragi kali", "mudde"),
            names = mapOf("kn" to "ರಾಗಿ ಮುದ್ದೆ", "te" to "రాగి సంకటి", "ta" to "கேழ்வரகு உருண்டை")),
        item("jowar_roti", "Jowar Roti", WEST, VEGAN, LUNCH, piece, 120, 3f, 25f, 1f, 3f, ROASTED,
            confidence = HIGH, ingredients = listOf("sorghum flour"),
            tags = listOf("homemade", "millet"),
            aliases = listOf("jowar bhakri", "jolada rotti", "bhakri", "sorghum roti"),
            names = mapOf("kn" to "ಜೋಳದ ರೊಟ್ಟಿ", "mr" to "ज्वारीची भाकरी", "hi" to "ज्वार रोटी")),
    )

    val byId: Map<String, FoodItem> = foods.associateBy { it.id }
}
