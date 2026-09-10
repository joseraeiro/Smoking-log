package com.example.smokinglog

import kotlin.random.Random

enum class HumorTopic { TODAY, HISTORY, INSIGHTS, SETTINGS }

/** Original cosmic absurdities, kept outside the UI so every screen can share the joke. */
object Humor {
    private fun combine(setups: List<String>, followUps: List<String>) =
        setups.flatMap { setup -> followUps.map { followUp -> "$setup $followUp" } }

    private val messages = mapOf(
        HumorTopic.TODAY to combine(
            listOf(
                "Nicotine has filed a flight plan, but you are under no obligation to clear it for take-off.",
                "Today's craving has arrived early and is pretending this makes it important.",
                "Your lungs have requested fewer plot twists and rather more oxygen.",
                "A cigarette break is still only a tea break with dreadful public relations.",
                "The urge insists it is an emergency, despite having brought neither sirens nor sandwiches.",
                "You needn't quit the whole galaxy today; one small, smoky asteroid will do.",
                "Your next cigarette has been delayed by a sudden outbreak of free will.",
                "The tobacco department regrets to announce that you have begun asking sensible questions.",
                "Today is mostly harmless; the cravings are merely auditioning for a larger role.",
                "A smoke-free hour has appeared unexpectedly and is trying not to look pleased with itself.",
            ),
            listOf(
                "This would be alarming if cravings were any good at paperwork.",
                "Fortunately, temporary chemical melodrama has a very short attention span.",
                "Remain calm and locate the nearest cup of something reassuring.",
                "The universe recommends waiting five minutes before believing any of it.",
                "No heroic cape is required, although a towel remains sensible.",
                "Proceed one improbably ordinary decision at a time.",
            ),
        ),
        HumorTopic.HISTORY to combine(
            listOf(
                "The past cannot be edited, but this log can, which is frankly better engineering.",
                "Here lies the evidence, arranged by timestamp and innocent of moral judgement.",
                "Every entry is data, including the untidy ones wearing metaphorical dressing gowns.",
                "History repeats itself, but at least yours now leaves useful statistics.",
                "The archive remembers every cigarette and still misplaces the obvious moral.",
                "Yesterday had no idea it would later be reviewed by someone this organised.",
                "The ship's log contains facts, feelings, and several suspiciously smoky commas.",
                "Your previous self has submitted a report marked ‘best effort under peculiar conditions’.",
                "These entries are footprints left by habits that assumed nobody was taking notes.",
                "The chronology is accurate, even when the day itself was assembled incorrectly.",
            ),
            listOf(
                "Review it gently; hindsight has an unfairly powerful telescope.",
                "No tribunal has been convened, largely because the chairs were uncomfortable.",
                "The useful bit is the pattern, not the urge to argue with Tuesday.",
                "Accuracy beats perfection, especially in badly signposted sectors of the week.",
                "Observe, learn, and resist appointing the spreadsheet as emperor.",
                "The record is here to help, not to compose a disappointed speech.",
            ),
        ),
        HumorTopic.INSIGHTS to combine(
            listOf(
                "Numbers are opinions that learned to stand in straight lines.",
                "The chart knows where you've been and remains charmingly vague about destiny.",
                "A downward trend is simply gravity finally doing something helpful.",
                "Statistics cannot quit for you, but they have volunteered to carry the clipboard.",
                "Patterns appear when chaos is persuaded to complete the correct forms.",
                "Progress is rarely straight; space itself never managed that particular trick.",
                "The daily average has emerged from arithmetic looking surprised but confident.",
                "Your smoking events have formed a graph and elected a temporary spokesperson.",
                "The data has discovered a trend and is trying not to become insufferable about it.",
                "This dashboard turns yesterday's smoke into today's unusually well-labelled bars.",
            ),
            listOf(
                "Treat the result as a map, not a prophecy with expensive stationery.",
                "Any encouraging movement is valid, even if it arrives by scenic route.",
                "The Guide advises curiosity and strongly discourages wrestling the axes.",
                "One should never panic merely because a bar chart has opinions.",
                "Look for direction; perfection is currently unavailable in this star system.",
                "The mathematics is sober, even if Tuesday plainly was not.",
            ),
        ),
        HumorTopic.SETTINGS to combine(
            listOf(
                "Adjust the universe carefully; some options may contain improbability.",
                "A daily target is a compass, not an intergalactic parking fine.",
                "Export your data before it develops wanderlust and joins a travelling circus.",
                "Preferences are where free will goes to acquire toggle switches.",
                "Your data stays aboard this device and knows better than to phone strangers.",
                "Set a target kindly; tyrannical spreadsheets have dreadful staff retention.",
                "The configuration department has provided several knobs and no convincing supervision.",
                "This page controls the app, though thankfully not the fundamental constants of nature.",
                "Your chosen target may be altered without notifying the Galactic Planning Committee.",
                "The export button is an escape pod for facts with excellent comma discipline.",
            ),
            listOf(
                "Choose calmly; none of this will be on the final exam.",
                "A sensible setting today prevents an unnecessarily operatic problem tomorrow.",
                "The recommended configuration is whichever one you will actually use.",
                "If uncertain, select kindness and keep a towel within reach.",
                "Technology works best when it remembers that you are in charge.",
                "Save when ready; the universe can wait, as it often does badly.",
            ),
        ),
    )

    private val smokedMessages = combine(
        listOf(
            "The event has been entered into the log by clerks who deny having seen anything.",
            "Honesty remains an excellent navigation system, despite its alarming lack of cup holders.",
            "The universe continues, but now with one fewer unlabelled incident.",
            "A precise tap has defeated twelve optimistic guesses in formal combat.",
            "The cigarette has been counted and may no longer travel under an assumed name.",
            "Your log has accepted the evidence with the solemnity of a toaster receiving bread.",
            "The smoking event is now official, which has disappointed its plans for a mysterious past.",
            "A tiny bureaucrat inside the phone has stamped this entry ‘surprisingly candid’.",
            "The record now contains another fact and is already demanding a larger clipboard.",
            "This cigarette has joined the statistics, where all dramatic gestures become decimals.",
        ),
        listOf(
            "No judgement was available, so accuracy has been substituted.",
            "The paperwork is immaculate and almost certainly overqualified.",
            "Proceed normally; honesty has already done the difficult bit.",
            "The Guide recommends neither panic nor creative accounting.",
            "Tomorrow's chart will know what to do with it, more or less.",
            "It is not victory or defeat; it is useful data wearing sensible shoes.",
        ),
    )

    private val resistedMessages = combine(
        listOf(
            "The craving has been denied docking clearance and is circling the kitchen looking embarrassed.",
            "Your lungs have sent a thank-you card, though neither lung will admit choosing the glitter.",
            "A temporary chemical melodrama has been cancelled due to insufficient audience interest.",
            "The urge demanded immediate attention and received a glass of water instead.",
            "You have outwaited a craving, a creature famous for owning a watch but not understanding it.",
            "The cigarette that did not happen has been awarded invisibility with full ceremonial honours.",
            "Free will has made an unscheduled appearance and frightened several minor impulses.",
            "The nicotine committee called an emergency meeting and discovered you were not attending.",
            "You declined to smoke, causing the habit to reread the contract in mounting disbelief.",
            "One urge has passed without becoming an event, which is excellent project management.",
        ),
        listOf(
            "Somewhere, an unnecessarily official clipboard has acquired a gold star.",
            "This counts, even if no brass band was available at short notice.",
            "Remain casually proud; excessive smugness attracts forms in triplicate.",
            "The universe has recorded the point in your favour and misplaced the receipt.",
            "Take the win before causality notices and asks for additional identification.",
            "Nicely navigated; no cape, prophecy, or committee approval was required.",
        ),
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

    fun allSmokedFollowUps(): List<String> = smokedMessages

    fun allResistedCompliments(): List<String> = resistedMessages
}
