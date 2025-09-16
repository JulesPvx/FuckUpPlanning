package fr.uptrash.fuckupplanning.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.uptrash.fuckupplanning.data.model.KarmaTransaction
import fr.uptrash.fuckupplanning.data.model.KarmaTransactionType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KarmaRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    private val karmaTransactionsRef = database.getReference("karma_transactions")

    suspend fun addKarmaTransaction(transaction: KarmaTransaction): Result<String> {
        return try {
            val key = karmaTransactionsRef.push().key ?: throw Exception("Failed to generate key")
            karmaTransactionsRef.child(key).setValue(transaction.copy(id = key)).await()
            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserTotalKarma(userId: String): Result<Int> {
        return try {
            val snapshot = karmaTransactionsRef
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()

            var totalKarma = 0
            for (child in snapshot.children) {
                child.getValue(KarmaTransaction::class.java)?.let { transaction ->
                    totalKarma += transaction.karmaChange
                }
            }
            Result.success(totalKarma)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserTotalKarma(userId: String): Flow<Int> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalKarma = 0
                for (child in snapshot.children) {
                    child.getValue(KarmaTransaction::class.java)?.let { transaction ->
                        if (transaction.userId == userId) {
                            totalKarma += transaction.karmaChange
                        }
                    }
                }
                trySend(totalKarma)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        karmaTransactionsRef.addValueEventListener(listener)
        awaitClose { karmaTransactionsRef.removeEventListener(listener) }
    }

    suspend fun getKarmaTransactionsForHomework(homeworkId: String): Result<List<KarmaTransaction>> {
        return try {
            val snapshot = karmaTransactionsRef
                .orderByChild("homeworkId")
                .equalTo(homeworkId)
                .get()
                .await()

            val transactions = mutableListOf<KarmaTransaction>()
            for (child in snapshot.children) {
                child.getValue(KarmaTransaction::class.java)?.let { transaction ->
                    transactions.add(transaction.copy(id = child.key ?: ""))
                }
            }
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeVoteTransaction(
        homeworkId: String,
        voterUserId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val snapshot = karmaTransactionsRef
                .orderByChild("homeworkId")
                .equalTo(homeworkId)
                .get()
                .await()

            for (child in snapshot.children) {
                child.getValue(KarmaTransaction::class.java)?.let { transaction ->
                    if (transaction.voterUserId == voterUserId &&
                        transaction.userId == userId &&
                        transaction.type == KarmaTransactionType.VOTE
                    ) {
                        child.ref.removeValue().await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
