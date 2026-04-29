package com.example.mobiledev.data.repository

import com.example.mobiledev.data.mock.ActivityMetricType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class ActivityMiniStat(
    val label: String,
    val value: String
)

data class ActivitySummary(
    val title: String,
    val description: String,
    val value: String,
    val period: String,
    val type: ActivityMetricType
)

interface ActivityRepository {
    fun getMiniStatsStream(): Flow<List<ActivityMiniStat>>
    fun getSummariesStream(): Flow<List<ActivitySummary>>
}

class FirebaseActivityRepository(private val db: FirebaseDatabase) : ActivityRepository {
    
    private val activityStatsRef = db.getReference("activity_stats")

    override fun getMiniStatsStream(): Flow<List<ActivityMiniStat>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stats = snapshot.child("mini_stats").children.mapNotNull {
                    ActivityMiniStat(
                        label = it.child("label").getValue(String::class.java).orEmpty(),
                        value = it.child("value").getValue(String::class.java).orEmpty()
                    )
                }
                trySend(stats)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        activityStatsRef.addValueEventListener(listener)
        awaitClose { activityStatsRef.removeEventListener(listener) }
    }

    override fun getSummariesStream(): Flow<List<ActivitySummary>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val summaries = snapshot.child("summaries").children.mapNotNull {
                    val typeStr = it.child("type").getValue(String::class.java).orEmpty()
                    val type = runCatching { ActivityMetricType.valueOf(typeStr) }.getOrDefault(ActivityMetricType.AUTH)
                    ActivitySummary(
                        title = it.child("title").getValue(String::class.java).orEmpty(),
                        description = it.child("description").getValue(String::class.java).orEmpty(),
                        value = it.child("value").getValue(String::class.java).orEmpty(),
                        period = it.child("period").getValue(String::class.java).orEmpty(),
                        type = type
                    )
                }
                trySend(summaries)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        activityStatsRef.addValueEventListener(listener)
        awaitClose { activityStatsRef.removeEventListener(listener) }
    }
}
