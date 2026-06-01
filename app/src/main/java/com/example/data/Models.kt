package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Young Learner",
    val selectedSkin: String = "classic_hero", // classic_hero, cosmic_ranger, fire_knight, rainbow_unicorn
    val coins: Int = 0,
    val totalScore: Int = 0,
    val currentLevel: Int = 1,
    val unlockedSkins: String = "classic_hero", // Comma separated list
    val gameDifficulty: String = "Easy" // Easy, Medium, Hard
) {
    fun isSkinUnlocked(skinId: String): Boolean {
        return unlockedSkins.split(",").contains(skinId)
    }
}

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String, // e.g. "Completed Quiz", "Unlocked Skin", "Failed Match Card Game"
    val details: String, // e.g. "Class 1: Math - Chapter 1 (Score: 3/3)"
    val pointsEarned: Int = 0,
    val coinsEarned: Int = 0
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val score: Int,
    val isRealPlayer: Boolean = false,
    val avatarSkin: String = "classic_hero"
)

@Entity(tableName = "chapter_progress")
data class ChapterProgress(
    @PrimaryKey val progressId: String, // format: "classId_subId_chapId"
    val classId: String,
    val subjectId: String,
    val chapterId: String,
    val quizCompleted: Boolean = false,
    val quizScore: Int = 0,
    val matchingCompleted: Boolean = false,
    val matchingScore: Int = 0,
    val motionCompleted: Boolean = false,
    val motionScore: Int = 0
)

// In-Memory Representation of Curriculum
data class GameClass(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val rawColor: Long // Hex color for custom styling
)

data class Subject(
    val id: String,
    val name: String,
    val description: String,
    val rawColor: Long,
    val icon: String
)

data class Chapter(
    val id: String,
    val orderId: Int,
    val name: String,
    val rawColor: Long,
    // Custom payload for gameplay
    val quizQuestions: List<QuizQuestion>,
    val matchPairs: List<MatchPair>,
    val motionScrollVelocity: Float // Changes based on chapter / difficulty
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val funFact: String
)

data class MatchPair(
    val key: String,
    val value: String
)

data class CustomSkin(
    val id: String,
    val name: String,
    val cost: Int,
    val colorHex: Long,
    val trailColorHex: Long,
    val avatarEmoji: String,
    val abilityDescription: String
)

object CurriculumData {
    val skins = listOf(
        CustomSkin("classic_hero", "Classic Mario Red", 0, 0xFFE91E63, 0xFFFFCDD2, "🦁", "Standard runner with standard high hop!"),
        CustomSkin("cosmic_ranger", "Cosmic Blue", 120, 0xFF2196F3, 0xFFBBDEFB, "👾", "Glides nicely in the air!"),
        CustomSkin("fire_knight", "Fire Ember", 250, 0xFFFF5722, 0xFFFFCCBC, "🐲", "Dashes forward elegantly!"),
        CustomSkin("rainbow_unicorn", "Magical Spark", 500, 0xFF9C27B0, 0xFFE1BEE7, "🦄", "Sparkly stardust trail follows behind!")
    )

    val classes = listOf(
        GameClass("class_1", "Class 1", "Basics & Fun Discoveries", "🎨", 0xFFFFE082),
        GameClass("class_2", "Class 2", "Math Magic & Animal Science", "📐", 0xFFA5D6A7),
        GameClass("class_3", "Class 3", "Universe Mysteries & Quick Counting", "🚀", 0xFF90CAF9)
    )

    val subjects = mapOf(
        "class_1" to listOf(
            Subject("math", "Mathematics", "Shapes, Counting & Order", 0xFFFF8A80, "🔢"),
            Subject("science", "General Science", "Healthy Foods & Sun Solar", 0xFF80D8FF, "🍎"),
            Subject("english", "Alphabet Quest", "Capitalization & Rhymes", 0xFFCCFF90, "📚")
        ),
        "class_2" to listOf(
            Subject("math", "Mathematics", "Addition Magic & Grid Patterns", 0xFFFF8A80, "➕"),
            Subject("science", "Animal Science", "Habitats & Forest Wonders", 0xFFB9F6CA, "🐾"),
            Subject("english", "English Phonics", "Spelling Blocks & Opposites", 0xFFFFD180, "🔠")
        ),
        "class_3" to listOf(
            Subject("math", "Mathematics", "Multiplication Rush & Shapes Match", 0xFFFF9E80, "✖️"),
            Subject("science", "Space Mysteries", "Gravity, Solar System & Soil", 0xFFEA80FC, "🪐"),
            Subject("english", "English Fun Grammar", "Nouns, Verbs & Synonyms", 0xFF82B1FF, "💬")
        )
    )

    // Dynamic curriculum content depending on the choice
    val chapters = mapOf(
        "class_1_math" to listOf(
            Chapter(
                "chap_1", 1, "Fun Shapes & Sizes", 0xFFFFAB91,
                quizQuestions = listOf(
                    QuizQuestion("Choose the shape that has 3 corners:", listOf("Circle", "Triangle", "Square"), 1, "A triangle always has 3 sides and 3 corners!"),
                    QuizQuestion("Which shape looks like a fresh round donut?", listOf("Square", "Triangle", "Circle"), 2, "A circle is round like a wheel or a donut!"),
                    QuizQuestion("A book has 4 sides and look flat and wide. What shape is it?", listOf("Rectangle", "Triangle", "Circle"), 0, "Rectangles have four sides where opposite sides are equal!")
                ),
                matchPairs = listOf(
                    MatchPair("3 Corners", "Triangle"),
                    MatchPair("4 Equal Sides", "Square"),
                    MatchPair("Perfect Round", "Circle"),
                    MatchPair("Egg Shaped", "Oval")
                ),
                motionScrollVelocity = 3.5f
            ),
            Chapter(
                "chap_2", 2, "Counting Toys Block", 0xFFB2DFDB,
                quizQuestions = listOf(
                    QuizQuestion("You have 2 toy cars, and mommy gives you 1 more. How many now?", listOf("3 Cars", "4 Cars", "2 Cars"), 0, "2 plus 1 is 3! Good counting!"),
                    QuizQuestion("What number comes right after 5?", listOf("4", "6", "7"), 1, "Let's count: 1, 2, 3, 4, 5, 6!"),
                    QuizQuestion("There are 5 birds. 2 of them fly away. How many birds are left?", listOf("2 Birds", "3 Birds", "4 Birds"), 1, "5 minus 2 is 3. Yes!")
                ),
                matchPairs = listOf(
                    MatchPair("One hand fingers", "5"),
                    MatchPair("A Pair of shoes", "2"),
                    MatchPair("Single Nose", "1"),
                    MatchPair("Spider legs", "8")
                ),
                motionScrollVelocity = 4.0f
            )
        ),
        "class_1_science" to listOf(
            Chapter(
                "chap_1", 1, "Power Fruits & Vegetables", 0xFFC5E1A5,
                quizQuestions = listOf(
                    QuizQuestion("Which energetic fruit keeps the doctor away every day?", listOf("Banana", "Apple", "Orange"), 1, "An Apple a day keeps the doctor away!"),
                    QuizQuestion("What sweet food do bees collect from flowers?", listOf("Honey", "Milk", "Cheese"), 0, "Bees make honey from flower nectar!"),
                    QuizQuestion("Rabbits love to munch on this orange vegetable:", listOf("Tomato", "Carrot", "Potato"), 1, "Carrots are super healthy for rabbit eyes!")
                ),
                matchPairs = listOf(
                    MatchPair("Monkeys Love", "Banana"),
                    MatchPair("Red & Crisp", "Apple"),
                    MatchPair("Orange & sweet", "Carrot"),
                    MatchPair("Spicy & green", "Chili")
                ),
                motionScrollVelocity = 3.8f
            )
        ),
        "class_1_english" to listOf(
            Chapter(
                "chap_1", 1, "Letter Sound Connects", 0xFFD1C4E9,
                quizQuestions = listOf(
                    QuizQuestion("Which animal starts with the letter 'E'?", listOf("Lion", "Elephant", "Dog"), 1, "E is for Elephant, the giant gentle animal!"),
                    QuizQuestion("Complete the pattern: A, B, C, D, ... ?", listOf("F", "E", "G"), 1, "Let's sing ABCD song: A, B, C, D, E!"),
                    QuizQuestion("What letter does 'APPLE' start with?", listOf("P", "B", "A"), 2, "A-P-P-L-E starts with A!")
                ),
                matchPairs = listOf(
                    MatchPair("A is for", "Apple"),
                    MatchPair("B is for", "Ball"),
                    MatchPair("C is for", "Cat"),
                    MatchPair("D is for", "Dog")
                ),
                motionScrollVelocity = 3.5f
            )
        ),

        "class_2_math" to listOf(
            Chapter(
                "chap_1", 1, "Magical Addition", 0xFFFFCC80,
                quizQuestions = listOf(
                    QuizQuestion("What is 10 + 15?", listOf("20", "25", "30"), 1, "10 plus 15 gives you 25!"),
                    QuizQuestion("Which combination equals exactly 12?", listOf("5 + 7", "6 + 5", "8 + 3"), 0, "5 + 7 is exactly 12!"),
                    QuizQuestion("If you double the number 8, what do you get?", listOf("14", "16", "18"), 1, "8 times 2, or 8 + 8 is 16!")
                ),
                matchPairs = listOf(
                    MatchPair("5 + 5", "10"),
                    MatchPair("9 + 9", "18"),
                    MatchPair("7 + 6", "13"),
                    MatchPair("12 + 12", "24")
                ),
                motionScrollVelocity = 4.2f
            )
        ),
        "class_2_science" to listOf(
            Chapter(
                "chap_1", 1, "Cute Forest Habitats", 0xFFE6EE9C,
                quizQuestions = listOf(
                    QuizQuestion("Where does a fluffy little bird lay its eggs?", listOf("Nest", "Cave", "Water"), 0, "Birds weave standard twig nests up in leafy trees!"),
                    QuizQuestion("This busy animal lives in an underground burrow:", listOf("Fish", "Rabbit", "Monic"), 1, "Rabbits dig complex underground burrows and warrens!"),
                    QuizQuestion("Which animal can live both under water and on green grass?", listOf("Frog", "Monkey", "Eagle"), 0, "Frogs are amphibians living on both land & water!")
                ),
                matchPairs = listOf(
                    MatchPair("Tree nests", "Birds"),
                    MatchPair("Honey Hive", "Bees"),
                    MatchPair("Dry Cave", "Bats"),
                    MatchPair("Water Reef", "Fish")
                ),
                motionScrollVelocity = 4.0f
            )
        ),
        "class_2_english" to listOf(
            Chapter(
                "chap_1", 1, "Opposite Word Blocks", 0xFFFFF59D,
                quizQuestions = listOf(
                    QuizQuestion("What is the opposite of high up in the SKY?", listOf("Low", "Tall", "Cloudy"), 0, "The opposite of High is Low!"),
                    QuizQuestion("Which option is the opposite of standard 'DARK'?", listOf("Black", "Light", "Shadow"), 1, "The opposite of Dark is Light!"),
                    QuizQuestion("What is the opposite of 'HAPPY' when you smile?", listOf("Glad", "Angry", "Sad"), 2, "The opposite of Happy is Sad!")
                ),
                matchPairs = listOf(
                    MatchPair("Hot", "Cold"),
                    MatchPair("Big", "Small"),
                    MatchPair("Day", "Night"),
                    MatchPair("Fast", "Slow")
                ),
                motionScrollVelocity = 3.6f
            )
        ),

        "class_3_math" to listOf(
            Chapter(
                "chap_1", 1, "Multiplication Rush", 0xFFFF9E80,
                quizQuestions = listOf(
                    QuizQuestion("What is 5 times 6?", listOf("30", "25", "35"), 0, "5 multiplied by 6 is 30!"),
                    QuizQuestion("If 4 times X is 24, what is X?", listOf("5", "6", "8"), 1, "4 times 6 is 24!"),
                    QuizQuestion("Multiply 9 by 3:", listOf("18", "21", "27"), 2, "9 times 3 is 27! Great math focus!")
                ),
                matchPairs = listOf(
                    MatchPair("3 x 4", "12"),
                    MatchPair("5 x 5", "25"),
                    MatchPair("7 x 8", "56"),
                    MatchPair("9 x 9", "81")
                ),
                motionScrollVelocity = 4.8f
            )
        ),
        "class_3_science" to listOf(
            Chapter(
                "chap_1", 1, "The Solar System", 0xFFB39DDB,
                quizQuestions = listOf(
                    QuizQuestion("Which planet is closest to our warm yellow Sun?", listOf("Earth", "Mercury", "Venus"), 1, "Mercury orbits closest to our giant Sun!"),
                    QuizQuestion("What gaseous planet has spectacular shiny visible rings around it?", listOf("Jupiter", "Mars", "Saturn"), 2, "Saturn has beautiful, shiny rings of ice and cosmic dust!"),
                    QuizQuestion("How many days does Mother Earth take to orbit Once around the Sun?", listOf("365 Days", "100 Days", "30 Days"), 0, "Earth takes exactly 1 year or 365 days to complete its orbit!")
                ),
                matchPairs = listOf(
                    MatchPair("Nearest to Sun", "Mercury"),
                    MatchPair("Red Planet", "Mars"),
                    MatchPair("Has Rings", "Saturn"),
                    MatchPair("Our Blue Home", "Earth")
                ),
                motionScrollVelocity = 5.2f
            )
        ),
        "class_3_english" to listOf(
            Chapter(
                "chap_1", 1, "Nouns & Verbs Blocks", 0xFF9FA8DA,
                quizQuestions = listOf(
                    QuizQuestion("Identify the action Verb in this sentence: 'The puppy jumped high!'", listOf("puppy", "jumped", "high"), 1, "Jumped is the physical action (Verb)!"),
                    QuizQuestion("Which word is a naming Noun representing a sweet tasty fruit?", listOf("Run", "Sweet", "Apple"), 2, "Apple is a Noun (names a fruit)!"),
                    QuizQuestion("Fill in the blank: 'We are ___ down the slide!'", listOf("sliding", "slide", "slid"), 0, "We are sliding down the slide!")
                ),
                matchPairs = listOf(
                    MatchPair("Naming Noun", "Elephant"),
                    MatchPair("Action Verb", "Running"),
                    MatchPair("Describing Adjective", "Beautiful"),
                    MatchPair("Joiner Conjunction", "Because")
                ),
                motionScrollVelocity = 4.2f
            )
        )
    )

    fun getChapterList(classId: String, subjectId: String): List<Chapter> {
        val compositeKey = "${classId}_$subjectId"
        return chapters[compositeKey] ?: emptyList()
    }

    fun getChapter(classId: String, subjectId: String, chapterId: String): Chapter? {
        val chapters = getChapterList(classId, subjectId)
        return chapters.find { it.id == chapterId }
    }
}
