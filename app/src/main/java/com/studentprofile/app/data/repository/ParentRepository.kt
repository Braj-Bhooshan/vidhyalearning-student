package com.studentprofile.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.studentprofile.app.domain.models.ParentAccount
import com.studentprofile.app.domain.models.StudentProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParentRepository @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val PREFS_NAME = "ParentPrefs"
        private const val KEY_PARENTS_JSON = "parents_json"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getParents(): List<ParentAccount> {
        val json = prefs.getString(KEY_PARENTS_JSON, null) ?: return emptyList()
        val arr = JSONArray(json)
        val result = mutableListOf<ParentAccount>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(jsonToParent(obj))
        }
        return result
    }

    fun saveParents(parents: List<ParentAccount>) {
        val arr = JSONArray()
        parents.forEach { arr.put(parentToJson(it)) }
        prefs.edit().putString(KEY_PARENTS_JSON, arr.toString()).apply()
    }

    fun getParent(parentId: String): ParentAccount? = getParents().firstOrNull { it.parentId == parentId }

    fun addOrUpdateParent(parent: ParentAccount) {
        val parents = getParents().toMutableList()
        val index = parents.indexOfFirst { it.parentId == parent.parentId }
        if (index >= 0) parents[index] = parent else parents.add(parent)
        saveParents(parents)
    }

    fun addChildToParent(parentId: String, child: StudentProfile): Boolean {
        val parents = getParents().toMutableList()
        val index = parents.indexOfFirst { it.parentId == parentId }
        if (index < 0) return false
        val parent = parents[index]
        if (parent.children.any { it.studentId == child.studentId }) return false
        val updated = parent.copy(children = parent.children + child)
        parents[index] = updated
        saveParents(parents)
        return true
    }

    fun getChildrenForParent(parentId: String): List<StudentProfile> = getParent(parentId)?.children ?: emptyList()

    fun findStudent(studentId: String): StudentProfile? {
        return getParents()
            .asSequence()
            .flatMap { it.children.asSequence() }
            .firstOrNull { it.studentId == studentId }
    }

    fun findParentByStudentId(studentId: String): ParentAccount? {
        return getParents().firstOrNull { parent -> parent.children.any { it.studentId == studentId } }
    }

    private fun parentToJson(parent: ParentAccount): JSONObject {
        val obj = JSONObject()
        obj.put("parentId", parent.parentId)
        obj.put("password", parent.password)
        val childrenArr = JSONArray()
        parent.children.forEach { childrenArr.put(studentToJson(it)) }
        obj.put("children", childrenArr)
        return obj
    }

    private fun jsonToParent(obj: JSONObject): ParentAccount {
        val parentId = obj.optString("parentId")
        val password = obj.optString("password")
        val childrenArr = obj.optJSONArray("children") ?: JSONArray()
        val children = mutableListOf<StudentProfile>()
        for (i in 0 until childrenArr.length()) {
            val childObj = childrenArr.getJSONObject(i)
            children.add(jsonToStudent(childObj))
        }
        return ParentAccount(parentId = parentId, password = password, children = children)
    }

    private fun studentToJson(s: StudentProfile): JSONObject {
        val obj = JSONObject()
        obj.put("studentId", s.studentId)
        obj.put("displayName", s.displayName)
        obj.put("classInfo", s.classInfo)
        s.section?.let { obj.put("section", it) }
        s.session?.let { obj.put("session", it) }
        s.admissionId?.let { obj.put("admissionId", it) }
        s.avatarResId?.let { obj.put("avatarResId", it) }
        obj.put("mpinRegistered", s.mpinRegistered)
        s.fatherName?.let { obj.put("fatherName", it) }
        s.motherName?.let { obj.put("motherName", it) }
        s.photoUrl?.let { obj.put("photoUrl", it) }
        return obj
    }

    private fun jsonToStudent(obj: JSONObject): StudentProfile {
        return StudentProfile(
            studentId = obj.optString("studentId"),
            displayName = obj.optString("displayName"),
            classInfo = obj.optString("classInfo"),
            section = if (obj.has("section")) obj.optString("section") else null,
            session = if (obj.has("session")) obj.optString("session") else null,
            admissionId = if (obj.has("admissionId")) obj.optString("admissionId") else null,
            avatarResId = if (obj.has("avatarResId")) obj.optInt("avatarResId") else null,
            mpinRegistered = obj.optBoolean("mpinRegistered", false),
            fatherName = if (obj.has("fatherName")) obj.optString("fatherName") else null,
            motherName = if (obj.has("motherName")) obj.optString("motherName") else null,
            photoUrl = if (obj.has("photoUrl")) obj.optString("photoUrl") else null
        )
    }
}
