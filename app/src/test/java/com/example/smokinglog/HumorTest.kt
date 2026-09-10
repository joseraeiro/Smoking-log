package com.example.smokinglog

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HumorTest {
    @Test fun `each screen has several distinct bulletins`() {
        HumorTopic.entries.forEach { topic ->
            val messages = Humor.allFor(topic)
            assertTrue("$topic needs at least 50 messages", messages.size >= 50)
            assertEquals(messages.size, messages.distinct().size)
        }
    }

    @Test fun `next bulletin does not immediately repeat`() {
        HumorTopic.entries.forEach { topic ->
            val previous = Humor.allFor(topic).first()
            repeat(20) {
                assertFalse(previous == Humor.next(topic, previous, Random(it)))
            }
        }
    }

    @Test fun `smoked message identifies the logged amount`() {
        assertTrue(Humor.smoked(1.0, Random(1)).startsWith("Whole cigarette logged."))
        assertTrue(Humor.smoked(.5, Random(1)).startsWith("Half cigarette logged."))
    }

    @Test fun `action feedback has sixty distinct possibilities each`() {
        val smoked = Humor.allSmokedFollowUps()
        val resisted = Humor.allResistedCompliments()
        assertEquals(60, smoked.size)
        assertEquals(60, smoked.distinct().size)
        assertEquals(60, resisted.size)
        assertEquals(60, resisted.distinct().size)
    }
}
