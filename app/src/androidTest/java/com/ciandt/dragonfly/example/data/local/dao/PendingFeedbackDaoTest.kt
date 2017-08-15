package com.ciandt.dragonfly.example.data.local.dao

import android.support.test.runner.AndroidJUnit4
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by iluz on 8/15/17.
 */
@RunWith(AndroidJUnit4::class)
class PendingFeedbackDaoTest : BaseDaoTest() {
    protected val dao by lazy { DATABASE.getPendingFeedbackDao() }

    @Before
    fun clear() {
        dao.clear()
    }

    @Test
    fun shouldInsertAndRetrieveFeedback() {
        dao.insert(FEEDBACK_1)


        val savedFeedbackList = dao.getLimitingTo(5)
        assertEquals(1, savedFeedbackList.size)

        val savedFeedback = savedFeedbackList.get(0)
        savedFeedback.feedback!!.let {
            assertEquals(FEEDBACK_1.id, it.id)
            assertEquals(FEEDBACK_1.actualLabel, it.actualLabel)
            assertEquals(FEEDBACK_1.imageLocalPath, it.imageLocalPath)
        }
    }

    @Test
    fun shouldLimitTheNumberOfItemsReturned() {
        dao.apply {
            insert(FEEDBACK_1)
            insert(FEEDBACK_2)
        }

        val savedFeedbackList = dao.getLimitingTo(1)
        assertEquals(1, savedFeedbackList.size)

        val savedFeedback = savedFeedbackList.get(0)
        savedFeedback.feedback!!.let {
            assertEquals(FEEDBACK_1.id, it.id)
        }
    }

    @Test
    fun shouldReturnOlderItemsFirst() {
        dao.apply {
            insert(FEEDBACK_1)
            insert(FEEDBACK_2)
        }

        val savedFeedbackList = dao.getLimitingTo(1)
        assertEquals(1, savedFeedbackList.size)

        val savedFeedback = savedFeedbackList.get(0)
        savedFeedback.feedback!!.let {
            assertEquals(FEEDBACK_1.id, it.id)
        }
    }

    @Test
    fun shouldReturnEmptyListIfNoItemsWereCreated() {
        val savedFeedbackList = dao.getLimitingTo(1)
        assertTrue(savedFeedbackList.isEmpty())
    }

    @Test
    fun shouldClearAllItems() {
        dao.apply {
            insert(FEEDBACK_1)
            insert(FEEDBACK_2)
            clear()
        }

        val savedFeedbackList = dao.getLimitingTo(1)
        assertTrue(savedFeedbackList.isEmpty())
    }

    companion object {
        val FEEDBACK_1 = PendingFeedbackEntitiy(
                id = "1234",
                actualLabel = "sunflower",
                imageLocalPath = "/path/to/image/1234",
                createdAt = 10
        )

        val FEEDBACK_2 = PendingFeedbackEntitiy(
                id = "4567",
                actualLabel = "spiderlilly",
                imageLocalPath = "/path/to/image/4567",
                createdAt = 20
        )
    }
}