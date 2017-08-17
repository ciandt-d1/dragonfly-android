package com.ciandt.dragonfly.example.data.local.dao

import android.support.test.runner.AndroidJUnit4
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by iluz on 8/15/17.
 */
@RunWith(AndroidJUnit4::class)
class PendingFeedbackLabelDaoTest : BaseDaoTest() {
    protected val dao by lazy { DATABASE.getPendingFeedbackLabelDao() }

    @Before
    fun clear() {
        dao.clear()
    }

    @Test
    fun shouldGenerateIdOnInsertion() {
        val feedback = FEEDBACK_1_LABEL_1.copy()
        dao.insert(feedback)

        val pendingFeedbackLabelList = dao.findByFeedbackId(FEEDBACK_1_ID);
        assertEquals(1, pendingFeedbackLabelList.size)
        assertNotNull(pendingFeedbackLabelList.get(0).id)
    }

    @Test
    fun shouldFindByFeedbackId() {
        dao.apply {
            insert(FEEDBACK_1_LABEL_1.copy())
            insert(FEEDBACK_1_LABEL_2.copy())
            insert(FEEDBACK_2_LABEL_1.copy())
            insert(FEEDBACK_2_LABEL_2.copy())
        }

        val pendingFeedbackLabelList = dao.findByFeedbackId(FEEDBACK_2_ID);

        assertEquals(2, pendingFeedbackLabelList.size)
        pendingFeedbackLabelList.forEach {
            assertEquals(FEEDBACK_2_ID, it.feedbackId)
        }
    }

    @Test
    fun shouldReturnEmptyListIfFeedbackIdDoesNotExist() {
        val pendingFeedbackLabelList = dao.findByFeedbackId(FEEDBACK_INVALID_ID);
        assertTrue(pendingFeedbackLabelList.isEmpty())
    }

    @Test
    fun shouldDeleteByFeedbackId() {
        dao.apply {
            insert(FEEDBACK_1_LABEL_1.copy())
            insert(FEEDBACK_1_LABEL_2.copy())
            insert(FEEDBACK_2_LABEL_1.copy())
            insert(FEEDBACK_2_LABEL_2.copy())
            delete(FEEDBACK_1_ID)
        }

        val pendingFeedbackLabelList = listOf<PendingFeedbackLabelEntitiy>()
                .plus(dao.findByFeedbackId(FEEDBACK_1_ID))
                .plus(dao.findByFeedbackId(FEEDBACK_2_ID))

        assertEquals(2, pendingFeedbackLabelList.size)
        pendingFeedbackLabelList.forEach {
            assertEquals(FEEDBACK_2_ID, it.feedbackId)
        }
    }

    @Test
    fun shouldClearAllItems() {
        dao.apply {
            insert(FEEDBACK_1_LABEL_1.copy())
            insert(FEEDBACK_1_LABEL_2.copy())
            insert(FEEDBACK_2_LABEL_1.copy())
            insert(FEEDBACK_2_LABEL_2.copy())
            clear()
        }

        val pendingFeedbackLabelList = listOf<PendingFeedbackLabelEntitiy>()
                .plus(dao.findByFeedbackId(FEEDBACK_1_ID))
                .plus(dao.findByFeedbackId(FEEDBACK_2_ID))

        assertTrue(pendingFeedbackLabelList.isEmpty())
    }

    companion object {
        val FEEDBACK_INVALID_ID = "-1"
        val FEEDBACK_1_ID = "1"
        val FEEDBACK_2_ID = "2"

        val FEEDBACK_1_LABEL_1 = PendingFeedbackLabelEntitiy(
                feedbackId = FEEDBACK_1_ID,
                label = "feedback 1 - label 1",
                confidence = 0.50f
        )

        val FEEDBACK_1_LABEL_2 = PendingFeedbackLabelEntitiy(
                feedbackId = FEEDBACK_1_ID,
                label = "feedback 1 - label 2",
                confidence = 0.30f
        )

        val FEEDBACK_2_LABEL_1 = PendingFeedbackLabelEntitiy(
                feedbackId = FEEDBACK_2_ID,
                label = "feedback 2 - label 1",
                confidence = 0.60f
        )

        val FEEDBACK_2_LABEL_2 = PendingFeedbackLabelEntitiy(
                feedbackId = FEEDBACK_2_ID,
                label = "feedback 2 - label 2",
                confidence = 0.75f
        )
    }
}