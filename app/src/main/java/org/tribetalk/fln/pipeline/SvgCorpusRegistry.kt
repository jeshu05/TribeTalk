package org.tribetalk.fln.pipeline

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Metadata for a reusable visual primitive in the TribeTalk SVG Corpus.
 */
data class SvgAssetMetadata(
    val id: String,
    val objectKey: String,
    val santaliName: String,
    val hindiName: String,
    val englishName: String,
    val category: String,
    val relativeFilePath: String,
    val viewBox: String = "0 0 100 100",
    val tags: List<String> = emptyList(),
    val ageSuitability: List<String> = listOf("BALVATIKA", "GRADE_1", "GRADE_2"),
    val supportsMonochrome: Boolean = true,
    val license: String = "CC0 Public Domain"
)

/**
 * In-memory Registry and Semantic Index for the TribeTalk SVG Corpus.
 */
object SvgCorpusRegistry {

    private val registry = LinkedHashMap<String, SvgAssetMetadata>()
    private var isInitialized = false

    /**
     * Initializes the registry from assets/fln_svg_corpus/manifest.json.
     * Thread-safe and idempotent.
     */
    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        try {
            val jsonStr = context.assets.open("fln_svg_corpus/manifest.json").bufferedReader().use { it.readText() }
            loadFromJsonString(jsonStr)
            isInitialized = true
        } catch (e: Exception) {
            // Fallback: populate core primitives if asset read fails (e.g. in unit test without Android context)
            populateBuiltinDefaults()
            isInitialized = true
        }
    }

    /**
     * Parses JSON array string into the registry.
     */
    fun loadFromJsonString(jsonString: String) {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val tags = mutableListOf<String>()
            val tagsArr = obj.optJSONArray("tags")
            if (tagsArr != null) {
                for (j in 0 until tagsArr.length()) {
                    tags.add(tagsArr.getString(j))
                }
            }

            val ages = mutableListOf<String>()
            val agesArr = obj.optJSONArray("ageSuitability")
            if (agesArr != null) {
                for (j in 0 until agesArr.length()) {
                    ages.add(agesArr.getString(j))
                }
            }

            val meta = SvgAssetMetadata(
                id = obj.getString("id"),
                objectKey = obj.getString("objectKey").lowercase(),
                santaliName = obj.optString("santaliName", ""),
                hindiName = obj.optString("hindiName", ""),
                englishName = obj.optString("englishName", ""),
                category = obj.optString("category", "general"),
                relativeFilePath = obj.getString("relativeFilePath"),
                viewBox = obj.optString("viewBox", "0 0 100 100"),
                tags = tags,
                ageSuitability = ages,
                supportsMonochrome = obj.optBoolean("supportsMonochrome", true),
                license = obj.optString("license", "CC0")
            )
            registry[meta.objectKey] = meta
        }
    }

    /**
     * Retrieves metadata by objectKey (e.g. "mango", "clay_pot").
     */
    fun get(objectKey: String): SvgAssetMetadata? {
        if (!isInitialized) populateBuiltinDefaults()
        return registry[objectKey.lowercase()]
    }

    /**
     * Retrieves all items in a category (e.g. "fruits", "animals", "tokens", "math").
     */
    fun getByCategory(category: String): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.values.filter { it.category.equals(category, ignoreCase = true) }
    }

    /**
     * Retrieves all items suitable for a specific NIPUN grade level.
     */
    fun getByGrade(grade: org.tribetalk.fln.model.FlnGrade): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        val gradeName = grade.name
        return registry.values.filter { it.ageSuitability.contains(gradeName) }
    }

    /**
     * Retrieves math operators (+, -, *, /, =).
     */
    fun getMathOperators(): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.values.filter { it.tags.contains("operator") }
    }

    /**
     * Retrieves Indian currency tokens (₹1, ₹2, ₹5, ₹10 coins and notes).
     */
    fun getCurrencyTokens(): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.values.filter { it.tags.contains("money") }
    }

    /**
     * Retrieves 2D geometric shapes (circle, square, triangle, rectangle, star).
     */
    fun getGeometricShapes(): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.values.filter { it.tags.contains("shape") || it.tags.contains("geometry") }
    }

    /**
     * Retrieves suitable counting tokens (e.g. pebble, leaf, mango, pot, dot, counter).
     */
    fun getCountingTokens(): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.values.filter { it.tags.contains("counting_token") || it.tags.contains("counter") }
    }

    /**
     * Searches items by tag or name.
     */
    fun search(query: String): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        val q = query.lowercase().trim()
        return registry.values.filter {
            it.objectKey.contains(q) ||
            it.santaliName.contains(q) ||
            it.hindiName.contains(q) ||
            it.englishName.contains(q, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(q) }
        }
    }

    /**
     * Returns all registered semantic object keys.
     */
    fun getAllKeys(): List<String> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.keys.toList()
    }

    /**
     * Finds the best matching object key for a query string, including inflections.
     */
    fun findMatchingKey(query: String): String? {
        if (!isInitialized) populateBuiltinDefaults()
        val q = query.lowercase().trim()
        if (registry.containsKey(q)) return q

        // Direct search
        val directMatch = search(q).firstOrNull()
        if (directMatch != null) return directMatch.objectKey

        // Stem matching for inflections: Hindi plurals / case forms (e.g. "घड़े" -> "घड़", "आमों" -> "आम")
        val stems = when {
            q.endsWith("ों") -> listOf(q.dropLast(2))
            q.endsWith("े") || q.endsWith("ा") || q.endsWith("ी") -> listOf(q.dropLast(1))
            else -> emptyList()
        }
        for (stem in stems) {
            if (stem.length >= 2) {
                val stemMatch = registry.values.firstOrNull {
                    it.hindiName.contains(stem) || it.santaliName.contains(stem)
                }
                if (stemMatch != null) return stemMatch.objectKey
            }
        }

        // Common keyword mappings
        if (q.contains("जोड़") || q.contains("add") || q.contains("plus") || q.contains("ᱡᱚᱲ")) return "plus"
        if (q.contains("घटा") || q.contains("sub") || q.contains("minus") || q.contains("ᱵᱷᱮᱜᱟᱨ")) return "minus"
        if (q.contains("गुणा") || q.contains("multi") || q.contains("times") || q.contains("ᱜᱩᱬᱟᱹ")) return "multiply"
        if (q.contains("भाग") || q.contains("divide") || q.contains("ᱦᱟᱹᱴᱤᱧ")) return "divide"
        if (q.contains("बराबर") || q.contains("equals") || q.contains("ᱥᱚᱢᱟᱱ")) return "equals"
        if (q.contains("रुपय") || q.contains("पैसा") || q.contains("सिक्का") || q.contains("coin") || q.contains("money") || q.contains("ᱴᱟᱠᱟ")) return "coin_5"
        if (q.contains("मटका") || q.contains("घड़ा") || q.contains("कुल्हड़") || q.contains("ᱴᱩᱠᱩᱡ")) return "clay_pot"
        if (q.contains("आम") || q.contains("mango") || q.contains("ᱩᱞ")) return "mango"
        if (q.contains("केला") || q.contains("banana") || q.contains("ᱠᱟᱭᱨᱟ")) return "banana"
        if (q.contains("सेब") || q.contains("apple") || q.contains("ᱟᱯᱮᱞ")) return "apple"
        if (q.contains("संतरा") || q.contains("orange") || q.contains("ᱠᱚᱢᱞᱟ")) return "orange"
        if (q.contains("पपीता") || q.contains("papaya") || q.contains("ᱚᱢᱨᱤᱛ")) return "papaya"
        if (q.contains("अमरूद") || q.contains("guava") || q.contains("ᱟᱢᱨᱩᱫᱽ")) return "guava"
        if (q.contains("अंगूर") || q.contains("grapes") || q.contains("ᱟᱝᱜᱩᱨ")) return "grapes"
        if (q.contains("तरबूज") || q.contains("watermelon") || q.contains("ᱛᱚᱨᱵᱩᱡᱽ")) return "watermelon"
        if (q.contains("पत्ता") || q.contains("leaf") || q.contains("ᱥᱟᱠᱟᱢ")) return "sal_leaf"
        if (q.contains("पेड़") || q.contains("tree") || q.contains("ᱫᱟᱨᱮ")) return "tree"
        if (q.contains("किताब") || q.contains("book") || q.contains("ᱯᱚᱛᱚᱵ") || q.contains("ᱯᱩᱛᱷᱤ")) return "book"
        if (q.contains("स्लेट") || q.contains("slate") || q.contains("ᱥᱞᱮᱴ")) return "slate"
        if (q.contains("पेंसिल") || q.contains("pencil") || q.contains("ᱯᱮᱱᱥᱤᱞ")) return "pencil"
        if (q.contains("बस्ता") || q.contains("bag") || q.contains("ᱛᱷᱟᱹᱞᱤ")) return "bag"
        if (q.contains("घंटी") || q.contains("bell") || q.contains("ᱜᱷᱟᱹᱱᱴᱤ")) return "bell"
        if (q.contains("स्कूल") || q.contains("विद्यालय") || q.contains("school") || q.contains("ᱟᱥᱲᱟ")) return "school"
        if (q.contains("गेंद") || q.contains("ball") || q.contains("ᱵᱚᱞ")) return "ball"
        if (q.contains("पतंग") || q.contains("kite") || q.contains("ᱜᱩᱰᱤ")) return "kite"
        if (q.contains("सूरज") || q.contains("sun") || q.contains("ᱥᱤᱧ") || q.contains("ᱪᱟᱸᱫᱚ")) return "sun"
        if (q.contains("चाँद") || q.contains("moon") || q.contains("ᱧᱤᱫᱟᱹ")) return "moon"
        if (q.contains("तारा") || q.contains("star") || q.contains("ᱤᱯᱤᱞ")) return "star"
        if (q.contains("पहाड़") || q.contains("mountain") || q.contains("ᱵᱩᱨᱩ")) return "mountain"
        if (q.contains("नदी") || q.contains("river") || q.contains("ᱜᱟᱰᱟ") || q.contains("ᱱᱟᱹᱭ")) return "river"
        if (q.contains("पानी") || q.contains("water") || q.contains("ᱫᱟᱜ")) return "water"
        if (q.contains("बादल") || q.contains("cloud") || q.contains("ᱨᱤᱢᱤᱞ")) return "cloud"
        if (q.contains("आग") || q.contains("fire") || q.contains("ᱥᱮᱸᱜᱮᱞ")) return "fire"
        if (q.contains("धरती") || q.contains("earth") || q.contains("ᱚᱛ")) return "earth"
        if (q.contains("फूल") || q.contains("flower") || q.contains("ᱵᱟᱦᱟ")) return "flower"
        if (q.contains("अंडा") || q.contains("egg") || q.contains("ᱵᱤᱞᱤ")) return "egg"
        if (q.contains("रोटी") || q.contains("bread") || q.contains("roti") || q.contains("ᱨᱩᱴᱤ")) return "roti"
        if (q.contains("दूध") || q.contains("milk") || q.contains("ᱛᱚᱣᱟ")) return "milk"
        if (q.contains("चावल") || q.contains("भात") || q.contains("rice") || q.contains("ᱫᱟᱠᱟ")) return "bowl_rice"
        if (q.contains("कुत्ता") || q.contains("dog") || q.contains("ᱥᱮᱛᱟ")) return "dog"
        if (q.contains("बिल्ली") || q.contains("cat") || q.contains("ᱵᱤᱞᱟᱹᱭ") || q.contains("ᱯᱩᱥᱤ")) return "cat"
        if (q.contains("गाय") || q.contains("cow") || q.contains("ᱜᱟᱹᱭ")) return "cow"
        if (q.contains("बैल") || q.contains("bull") || q.contains("ox") || q.contains("ᱰᱟᱝᱜᱽᱨᱟ")) return "bull"
        if (q.contains("बाघ") || q.contains("tiger") || q.contains("ᱛᱟᱹᱨᱩᱵ")) return "tiger"
        if (q.contains("भालू") || q.contains("bear") || q.contains("ᱵᱟᱱᱟ")) return "bear"
        if (q.contains("घोड़ा") || q.contains("horse") || q.contains("ᱥᱟᱫᱚᱢ")) return "horse"
        if (q.contains("हाथी") || q.contains("elephant") || q.contains("ᱦᱟᱹᱛᱤ")) return "elephant"
        if (q.contains("बकरी") || q.contains("goat") || q.contains("ᱢᱮᱨᱚᱢ")) return "goat"
        if (q.contains("भेड़") || q.contains("sheep") || q.contains("ᱵᱷᱮᱰᱟ")) return "sheep"
        if (q.contains("हिरण") || q.contains("deer") || q.contains("ᱡᱤᱞ")) return "deer"
        if (q.contains("मेंढक") || q.contains("frog") || q.contains("ᱨᱚᱴᱮ")) return "frog"
        if (q.contains("बतख") || q.contains("duck") || q.contains("ᱜᱮᱰᱮ")) return "duck"
        if (q.contains("मुर्गी") || q.contains("hen") || q.contains("ᱥᱤᱢ")) return "hen"
        if (q.contains("मोर") || q.contains("peacock") || q.contains("ᱢᱟᱨᱟᱜ")) return "peacock"
        if (q.contains("चिड़िया") || q.contains("bird") || q.contains("ᱪᱮᱬᱮ")) return "bird"
        if (q.contains("तोता") || q.contains("parrot") || q.contains("ᱮᱨᱮ")) return "parrot"
        if (q.contains("तितली") || q.contains("butterfly") || q.contains("ᱯᱤᱯᱤᱲᱤᱭᱟᱹᱝ")) return "butterfly"
        if (q.contains("मछली") || q.contains("fish") || q.contains("ᱦᱟᱹᱠᱩ")) return "fish"
        if (q.contains("बंदर") || q.contains("monkey") || q.contains("ᱜᱟᱹᱰᱤ")) return "monkey"
        if (q.contains("खरगोश") || q.contains("rabbit") || q.contains("ᱠᱩᱞᱟᱹᱭ")) return "rabbit"
        if (q.contains("बच्चा") || q.contains("लड़का") || q.contains("बालक") || q.contains("child") || q.contains("student") || q.contains("kid") || q.contains("ᱜᱤᱫᱽᱨᱟᱹ")) return "child"
        if (q.contains("झोपड़ी") || q.contains("घर") || q.contains("hut") || q.contains("house") || q.contains("ᱚᱲᱟᱜ")) return "straw_hut"
        if (q.contains("मांदर") || q.contains("ढोल") || q.contains("drum") || q.contains("ᱛᱩᱢᱫᱟᱜ")) return "tumdak_drum"
        if (q.contains("बांसुरी") || q.contains("flute") || q.contains("ᱛᱤᱨᱤᱭᱟᱹᱣ")) return "flute"
        if (q.contains("धनुष") || q.contains("तीर") || q.contains("bow") || q.contains("arrow") || q.contains("ᱟᱜ")) return "bow_arrow"
        if (q.contains("हँसिया") || q.contains("sickle") || q.contains("ᱫᱟᱹᱛᱨᱟᱹ")) return "sickle"
        if (q.contains("कुआँ") || q.contains("well") || q.contains("ᱰᱟᱹᱰᱤ")) return "well"
        if (q.contains("टोकरी") || q.contains("basket") || q.contains("ᱰᱟᱹᱞᱤ")) return "basket"

        return null
    }

    /**
     * Returns all registered assets.
     */
    fun getAll(): List<SvgAssetMetadata> {
        if (!isInitialized) populateBuiltinDefaults()
        return registry.values.toList()
    }

    /**
     * Fallback for unit testing environments.
     */
    private fun populateBuiltinDefaults() {
        if (registry.isNotEmpty()) return
        val defaults = listOf(
            SvgAssetMetadata("svg_mango", "mango", "ᱩᱞ", "आम", "Mango", "fruits", "fln_svg_corpus/plants/mango.svg", tags = listOf("fruit", "counting_token")),
            SvgAssetMetadata("svg_banana", "banana", "ᱠᱟᱭᱨᱟ", "केला", "Banana", "fruits", "fln_svg_corpus/plants/banana.svg", tags = listOf("fruit", "counting_token")),
            SvgAssetMetadata("svg_apple", "apple", "ᱟᱯᱮᱞ", "सेब", "Apple", "fruits", "fln_svg_corpus/plants/apple.svg", tags = listOf("fruit", "counting_token")),
            SvgAssetMetadata("svg_orange", "orange", "ᱠᱚᱢᱞᱟ", "संतरा", "Orange", "fruits", "fln_svg_corpus/plants/orange.svg", tags = listOf("fruit", "counting_token")),
            SvgAssetMetadata("svg_grapes", "grapes", "ᱟᱝᱜᱩᱨ", "अंगूर", "Grapes", "fruits", "fln_svg_corpus/plants/grapes.svg", tags = listOf("fruit", "counting_token")),
            SvgAssetMetadata("svg_watermelon", "watermelon", "ᱛᱚᱨᱵᱩᱡᱽ", "तरबूज", "Watermelon", "fruits", "fln_svg_corpus/plants/watermelon.svg", tags = listOf("fruit")),
            SvgAssetMetadata("svg_tree", "tree", "ᱫᱟᱨᱮ", "पेड़", "Tree", "plants", "fln_svg_corpus/plants/tree.svg", tags = listOf("tree", "nature")),
            SvgAssetMetadata("svg_sal_leaf", "sal_leaf", "ᱥᱟᱨᱡᱚᱢ ᱥᱟᱠᱟᱢ", "साल का पत्ता", "Sal Leaf", "plants", "fln_svg_corpus/plants/sal_leaf.svg", tags = listOf("forest", "counting_token")),
            SvgAssetMetadata("svg_clay_pot", "clay_pot", "ᱴᱩᱠᱩᱡ", "घड़ा", "Clay Pot", "village_life", "fln_svg_corpus/village_life/clay_pot.svg", tags = listOf("household", "counting_token")),
            SvgAssetMetadata("svg_straw_hut", "straw_hut", "ᱚᱲᱟᱜ", "घर", "Straw Hut", "village_life", "fln_svg_corpus/village_life/straw_hut.svg", tags = listOf("home", "village")),
            SvgAssetMetadata("svg_tumdak_drum", "tumdak_drum", "ᱛᱩᱢᱫᱟᱜ", "मांदर (ढोल)", "Tumdak Drum", "village_life", "fln_svg_corpus/village_life/tumdak_drum.svg", tags = listOf("music", "culture")),
            SvgAssetMetadata("svg_flute", "flute", "ᱛᱤᱨᱤᱭᱟᱹᱣ", "बांसुरी", "Flute", "village_life", "fln_svg_corpus/village_life/flute.svg", tags = listOf("music", "culture")),
            SvgAssetMetadata("svg_bow_arrow", "bow_arrow", "ᱟᱜ ᱥᱟᱨ", "तीर-धनुष", "Bow & Arrow", "village_life", "fln_svg_corpus/village_life/bow_arrow.svg", tags = listOf("archery", "culture")),
            SvgAssetMetadata("svg_sickle", "sickle", "ᱫᱟᱹᱛᱨᱟᱹ", "हँसिया", "Sickle", "village_life", "fln_svg_corpus/village_life/sickle.svg", tags = listOf("farming", "harvest")),
            SvgAssetMetadata("svg_well", "well", "ᱰᱟᱹᱰᱤ", "कुआँ", "Well", "village_life", "fln_svg_corpus/village_life/well.svg", tags = listOf("water", "village")),
            SvgAssetMetadata("svg_basket", "basket", "ᱰᱟᱹᱞᱤ", "टोकरी", "Basket", "village_life", "fln_svg_corpus/village_life/basket.svg", tags = listOf("harvest", "counting_token")),
            SvgAssetMetadata("svg_tiger", "tiger", "ᱛᱟᱹᱨᱩᱵ", "बाघ", "Tiger", "animals", "fln_svg_corpus/animals/tiger.svg", tags = listOf("animal", "wild")),
            SvgAssetMetadata("svg_bear", "bear", "ᱵᱟᱱᱟ", "भालू", "Bear", "animals", "fln_svg_corpus/animals/bear.svg", tags = listOf("animal", "forest")),
            SvgAssetMetadata("svg_horse", "horse", "ᱥᱟᱫᱚᱢ", "घोड़ा", "Horse", "animals", "fln_svg_corpus/animals/horse.svg", tags = listOf("animal", "riding")),
            SvgAssetMetadata("svg_deer", "deer", "ᱡᱤᱞ", "हिरण", "Deer", "animals", "fln_svg_corpus/animals/deer.svg", tags = listOf("animal", "forest")),
            SvgAssetMetadata("svg_frog", "frog", "ᱨᱚᱴᱮ", "मेंढक", "Frog", "animals", "fln_svg_corpus/animals/frog.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_sheep", "sheep", "ᱵᱷᱮᱰᱟ", "भेड़", "Sheep", "animals", "fln_svg_corpus/animals/sheep.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_bull", "bull", "ᱰᱟᱝᱜᱽᱨᱟ", "बैल", "Bull", "animals", "fln_svg_corpus/animals/bull.svg", tags = listOf("animal", "farm")),
            SvgAssetMetadata("svg_parrot", "parrot", "ᱮᱨᱮ", "तोता", "Parrot", "animals", "fln_svg_corpus/animals/parrot.svg", tags = listOf("bird", "talking")),
            SvgAssetMetadata("svg_dog", "dog", "ᱥᱮᱛᱟ", "कुत्ता", "Dog", "animals", "fln_svg_corpus/animals/dog.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_fish", "fish", "ᱦᱟᱹᱠᱩ", "मछली", "Fish", "animals", "fln_svg_corpus/animals/fish.svg", tags = listOf("river", "counting_token")),
            SvgAssetMetadata("svg_cat", "cat", "ᱵᱤᱞᱟᱹᱭ", "बिल्ली", "Cat", "animals", "fln_svg_corpus/animals/cat.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_cow", "cow", "ᱜᱟᱹᱭ", "गाय", "Cow", "animals", "fln_svg_corpus/animals/cow.svg", tags = listOf("animal", "farm")),
            SvgAssetMetadata("svg_elephant", "elephant", "ᱦᱟᱹᱛᱤ", "हाथी", "Elephant", "animals", "fln_svg_corpus/animals/elephant.svg", tags = listOf("animal", "large")),
            SvgAssetMetadata("svg_monkey", "monkey", "ᱜᱟᱹᱰᱤ", "बंदर", "Monkey", "animals", "fln_svg_corpus/animals/monkey.svg", tags = listOf("animal", "tree")),
            SvgAssetMetadata("svg_rabbit", "rabbit", "ᱠᱩᱞᱟᱹᱭ", "खरगोश", "Rabbit", "animals", "fln_svg_corpus/animals/rabbit.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_goat", "goat", "ᱢᱮᱨᱚᱢ", "बकरी", "Goat", "animals", "fln_svg_corpus/animals/goat.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_duck", "duck", "ᱜᱮᱰᱮ", "बतख", "Duck", "animals", "fln_svg_corpus/animals/duck.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_hen", "hen", "ᱥᱤᱢ", "मुर्गी", "Hen", "animals", "fln_svg_corpus/animals/hen.svg", tags = listOf("animal", "counting_token")),
            SvgAssetMetadata("svg_peacock", "peacock", "ᱢᱟᱨᱟᱜ", "मोर", "Peacock", "animals", "fln_svg_corpus/animals/peacock.svg", tags = listOf("bird", "dance")),
            SvgAssetMetadata("svg_butterfly", "butterfly", "ᱯᱤᱯᱤᱲᱤᱭᱟᱹᱝ", "तितली", "Butterfly", "animals", "fln_svg_corpus/animals/butterfly.svg", tags = listOf("insect", "counting_token")),
            SvgAssetMetadata("svg_bird", "bird", "ᱪᱮᱬᱮ", "चिड़िया", "Bird", "animals", "fln_svg_corpus/animals/bird.svg", tags = listOf("bird", "counting_token")),
            SvgAssetMetadata("svg_mountain", "mountain", "ᱵᱩᱨᱩ", "पहाड़", "Mountain", "nature", "fln_svg_corpus/nature/mountain.svg", tags = listOf("nature", "landscape")),
            SvgAssetMetadata("svg_river", "river", "ᱜᱟᱰᱟ", "नदी", "River", "nature", "fln_svg_corpus/nature/river.svg", tags = listOf("nature", "water")),
            SvgAssetMetadata("svg_water", "water", "ᱫᱟᱜ", "पानी", "Water", "nature", "fln_svg_corpus/nature/water.svg", tags = listOf("nature", "liquid")),
            SvgAssetMetadata("svg_cloud", "cloud", "ᱨᱤᱢᱤᱞ", "बादल", "Cloud", "nature", "fln_svg_corpus/nature/cloud.svg", tags = listOf("nature", "sky")),
            SvgAssetMetadata("svg_fire", "fire", "ᱥᱮᱸᱜᱮᱞ", "आग", "Fire", "nature", "fln_svg_corpus/nature/fire.svg", tags = listOf("nature", "element")),
            SvgAssetMetadata("svg_earth", "earth", "ᱚᱛ", "धरती", "Earth", "nature", "fln_svg_corpus/nature/earth.svg", tags = listOf("nature", "ground")),
            SvgAssetMetadata("svg_child", "child", "ᱜᱤᱫᱽᱨᱟᱹ", "बच्चा", "Child", "people", "fln_svg_corpus/people/child.svg", tags = listOf("people", "student", "counting_token")),
            SvgAssetMetadata("svg_school", "school", "ᱤᱛᱩᱱ ᱟᱥᱲᱟ", "विद्यालय", "School", "school", "fln_svg_corpus/school_and_play/school.svg", tags = listOf("school", "study")),
            SvgAssetMetadata("svg_bag", "bag", "ᱛᱷᱟᱹᱞᱤ", "बस्ता", "Bag", "school", "fln_svg_corpus/school_and_play/bag.svg", tags = listOf("school", "study")),
            SvgAssetMetadata("svg_bell", "bell", "ᱜᱷᱟᱹᱱᱴᱤ", "घंटी", "Bell", "school", "fln_svg_corpus/school_and_play/bell.svg", tags = listOf("school", "sound")),
            SvgAssetMetadata("svg_book", "book", "ᱯᱚᱛᱚᱵ", "किताब", "Book", "school", "fln_svg_corpus/school_and_play/book.svg", tags = listOf("school", "study")),
            SvgAssetMetadata("svg_slate", "slate", "ᱥᱞᱮᱴ", "स्लेट", "Slate", "school", "fln_svg_corpus/school_and_play/slate.svg", tags = listOf("school", "study")),
            SvgAssetMetadata("svg_pencil", "pencil", "ᱯᱮᱱᱥᱤᱞ", "पेंसिल", "Pencil", "school", "fln_svg_corpus/school_and_play/pencil.svg", tags = listOf("school", "study")),
            SvgAssetMetadata("svg_ball", "ball", "ᱵᱚᱞ", "गेंद", "Ball", "play", "fln_svg_corpus/school_and_play/ball.svg", tags = listOf("play", "counting_token")),
            SvgAssetMetadata("svg_kite", "kite", "ᱜᱩᱰᱤ", "पतंग", "Kite", "play", "fln_svg_corpus/school_and_play/kite.svg", tags = listOf("play", "counting_token")),
            SvgAssetMetadata("svg_sun", "sun", "ᱥᱤᱧ ᱪᱟᱸᱫᱚ", "सूरज", "Sun", "tokens", "fln_svg_corpus/tokens/sun.svg", tags = listOf("nature", "counting_token")),
            SvgAssetMetadata("svg_moon", "moon", "ᱧᱤᱫᱟᱹ ᱪᱟᱸᱫᱚ", "चाँद", "Moon", "tokens", "fln_svg_corpus/tokens/moon.svg", tags = listOf("nature", "counting_token")),
            SvgAssetMetadata("svg_flower", "flower", "ᱵᱟᱦᱟ", "फूल", "Flower", "tokens", "fln_svg_corpus/tokens/flower.svg", tags = listOf("nature", "counting_token")),
            SvgAssetMetadata("svg_pebble", "pebble", "ᱫᱷᱤᱨᱤ", "पत्थर / कंकड़", "Pebble", "tokens", "fln_svg_corpus/tokens/pebble.svg", tags = listOf("math", "counting_token")),
            SvgAssetMetadata("svg_star", "star", "ᱤᱯᱤᱞ", "तारा", "Star", "tokens", "fln_svg_corpus/tokens/star.svg", tags = listOf("math", "counting_token")),
            SvgAssetMetadata("svg_circle", "circle", "ᱜᱩᱞᱟᱹᱴ", "वृत्त", "Circle", "tokens", "fln_svg_corpus/tokens/circle.svg", tags = listOf("shape", "math")),
            SvgAssetMetadata("svg_square", "square", "ᱪᱟᱹᱣᱠᱟᱹ", "वर्ग", "Square", "tokens", "fln_svg_corpus/tokens/square.svg", tags = listOf("shape", "math")),
            SvgAssetMetadata("svg_triangle", "triangle", "ᱯᱮᱠᱳᱬ", "त्रिभुज", "Triangle", "tokens", "fln_svg_corpus/tokens/triangle.svg", tags = listOf("shape", "math")),
            SvgAssetMetadata("svg_rectangle", "rectangle", "ᱟᱭᱛᱟᱠᱟᱨ", "आयत", "Rectangle", "math", "fln_svg_corpus/math/rectangle.svg", tags = listOf("shape", "math")),
            SvgAssetMetadata("svg_roti", "roti", "ᱨᱩᱴᱤ", "रोटी", "Roti", "tokens", "fln_svg_corpus/tokens/roti.svg", tags = listOf("food", "eating")),
            SvgAssetMetadata("svg_milk", "milk", "ᱛᱚᱣᱟ", "दूध", "Milk", "tokens", "fln_svg_corpus/tokens/milk.svg", tags = listOf("food", "drink")),
            SvgAssetMetadata("svg_bowl_rice", "bowl_rice", "ᱫᱟᱠᱟ", "भात", "Rice", "tokens", "fln_svg_corpus/tokens/bowl_rice.svg", tags = listOf("food", "counting_token")),
            SvgAssetMetadata("svg_egg", "egg", "ᱵᱤᱞᱤ", "अंडा", "Egg", "tokens", "fln_svg_corpus/tokens/egg.svg", tags = listOf("food", "counting_token")),
            SvgAssetMetadata("svg_dot", "dot", "ᱴᱩᱰᱟᱹᱜ", "बिंदु", "Dot", "math", "fln_svg_corpus/math/dot.svg", tags = listOf("counter", "math")),
            SvgAssetMetadata("svg_counter_chip", "counter_chip", "ᱞᱮᱠᱷᱟ ᱜᱩᱞᱟᱹᱴ", "गिनती चिप", "Counter Chip", "math", "fln_svg_corpus/math/counter_chip.svg", tags = listOf("counter", "math")),
            SvgAssetMetadata("svg_plus", "plus", "ᱡᱚᱲ", "जोड़", "Plus", "math", "fln_svg_corpus/math/plus.svg", tags = listOf("operator", "math")),
            SvgAssetMetadata("svg_minus", "minus", "ᱵᱷᱮᱜᱟᱨ", "घटाव", "Minus", "math", "fln_svg_corpus/math/minus.svg", tags = listOf("operator", "math")),
            SvgAssetMetadata("svg_multiply", "multiply", "ᱜᱩᱬᱟᱹ", "गुणा", "Multiply", "math", "fln_svg_corpus/math/multiply.svg", tags = listOf("operator", "math")),
            SvgAssetMetadata("svg_divide", "divide", "ᱦᱟᱹᱴᱤᱧ", "भाग", "Divide", "math", "fln_svg_corpus/math/divide.svg", tags = listOf("operator", "math")),
            SvgAssetMetadata("svg_equals", "equals", "ᱥᱚᱢᱟᱱ", "बराबर", "Equals", "math", "fln_svg_corpus/math/equals.svg", tags = listOf("operator", "math")),
            SvgAssetMetadata("svg_coin_1", "coin_1", "ᱢᱤᱫ ᱴᱟᱠᱟ", "१ रुपया", "1 Rupee", "math", "fln_svg_corpus/math/coin_1.svg", tags = listOf("money", "math")),
            SvgAssetMetadata("svg_coin_2", "coin_2", "ᱵᱟᱨ ᱴᱟᱠᱟ", "२ रुपये", "2 Rupee", "math", "fln_svg_corpus/math/coin_2.svg", tags = listOf("money", "math")),
            SvgAssetMetadata("svg_coin_5", "coin_5", "ᱢᱚᱬᱮ ᱴᱟᱠᱟ", "५ रुपये", "5 Rupee", "math", "fln_svg_corpus/math/coin_5.svg", tags = listOf("money", "math")),
            SvgAssetMetadata("svg_coin_10", "coin_10", "ᱜᱮᱞ ᱴᱟᱠᱟ", "१० रुपये", "10 Rupee", "math", "fln_svg_corpus/math/coin_10.svg", tags = listOf("money", "math")),
            SvgAssetMetadata("svg_rupee_note", "rupee_note", "ᱴᱟᱠᱟ ᱱᱳᱴ", "रुपया नोट", "Rupee Note", "math", "fln_svg_corpus/math/rupee_note.svg", tags = listOf("money", "math"))
        )
        defaults.forEach { registry[it.objectKey] = it }
    }
}
