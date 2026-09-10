package com.example.smokinglog

import kotlin.random.Random

enum class HumorTopic { TODAY, HISTORY, INSIGHTS, SETTINGS }

/** Original cosmic absurdities, kept outside the UI so every screen can share the joke. */
object Humor {
    private val messages = mapOf(
        HumorTopic.TODAY to listOf(
            "Nicotine has filed a flight plan. You are under no obligation to approve it.",
            "Today is mostly harmless. The cravings are merely being theatrical.",
            "A cigarette break is just a tea break with worse public relations.",
            "Your lungs have requested fewer plot twists and rather more oxygen.",
            "The next craving may feel infinite. Fortunately, cravings are terrible at mathematics.",
            "You needn't quit the whole galaxy today. One small planet will do.",
        ),
        HumorTopic.HISTORY to listOf(
            "The past cannot be edited, but this log can. Much more convenient.",
            "Here lies the evidence, arranged by timestamp and innocent of moral judgement.",
            "Every entry is data. Even the untidy ones wearing dressing gowns.",
            "History repeats itself, but at least now it leaves useful statistics.",
            "The archive remembers everything except where you left your towel.",
            "Observe yesterday gently; it had no idea you would become this organised.",
        ),
        HumorTopic.INSIGHTS to listOf(
            "Numbers are opinions that learned to stand in straight lines.",
            "The chart knows where you've been. It remains charmingly vague about destiny.",
            "A downward trend is simply gravity finally doing something helpful.",
            "Statistics cannot quit for you, but they can carry the clipboard.",
            "Patterns appear when chaos is persuaded to fill in the correct forms.",
            "Progress is rarely a straight line; space itself couldn't manage that trick.",
        ),
        HumorTopic.SETTINGS to listOf(
            "Adjust the universe carefully. Some options may contain improbability.",
            "A daily target is a compass, not an intergalactic parking fine.",
            "Export your data before it develops wanderlust.",
            "Preferences are where free will goes to acquire toggle switches.",
            "Your data stays aboard this device and knows better than to phone strangers.",
            "Set a target kindly. Tyrannical spreadsheets have poor staff retention.",
        ),
    )

    private val smokedMessages = listOf(
        "Honesty remains an excellent navigation system.",
        "Recorded without judgement, paperwork, or poetry of unusual cruelty.",
        "The universe continues, but now with better data.",
        "One accurate tap beats twelve optimistic guesses.",
    )

    private val resistedMessages = listOf(
        "Victory logged. The craving has been informed that it is not management.",
        "Nicely navigated. Your lungs have sent a very small thank-you card.",
        "Urge resisted. Somewhere, a clipboard has acquired a gold star.",
        "Recorded: you, one; temporary chemical melodrama, nil.",
    )

    fun next(topic: HumorTopic, previous: String? = null, random: Random = Random.Default): String {
        val choices = messages.getValue(topic)
        val eligible = choices.filterNot { it == previous }.ifEmpty { choices }
        return eligible[random.nextInt(eligible.size)]
    }

    fun smoked(amount: Double, random: Random = Random.Default): String =
        "${if (amount == 1.0) "Whole cigarette" else "Half cigarette"} logged. ${smokedMessages.random(random)}"

    fun resisted(random: Random = Random.Default): String = resistedMessages.random(random)

    fun allFor(topic: HumorTopic): List<String> = messages.getValue(topic)
}
