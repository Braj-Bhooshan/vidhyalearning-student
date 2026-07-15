package com.studentprofile.app.data.local

import android.content.Context
import androidx.core.content.edit

// The parent assignments list carries no topic_id, so once a topic is resolved for an
// assignment (via taught-classes title matching) the mapping is cached here per device.
class AssignmentTopicPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("assignment_topic_prefs", Context.MODE_PRIVATE)

    fun getTopicId(assignmentId: Int): Int? =
        prefs.getInt(assignmentId.toString(), -1).takeIf { it >= 0 }

    fun putTopicId(assignmentId: Int, topicId: Int) {
        prefs.edit { putInt(assignmentId.toString(), topicId) }
    }
}
