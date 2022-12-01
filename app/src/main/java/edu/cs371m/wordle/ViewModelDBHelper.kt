package edu.cs371m.wordle

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewModelDBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val rootCollection = "allDictionaryMeta"

    fun fetchDictionaryMeta(notesList: MutableLiveData<List<DictionaryMeta>>) {
        dbFetchDictionaryMeta(notesList)
    }

    private fun limitAndGet(query: Query,
                            notesList: MutableLiveData<List<DictionaryMeta>>) {
        query
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "allNotes fetch ${result!!.documents.size}")
                notesList.postValue(result.documents.mapNotNull {
                    it.toObject(DictionaryMeta::class.java)
                })
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "allNotes fetch FAILED ", it)
            }
    }
    private fun dbFetchDictionaryMeta(notesList: MutableLiveData<List<DictionaryMeta>>) {
        limitAndGet(db.collection(rootCollection), notesList)
    }
    fun createDictionaryMeta(
        dictionaryMeta: DictionaryMeta,
        notesList: MutableLiveData<List<DictionaryMeta>>
    ) {
        db.collection(rootCollection)
            .add(dictionaryMeta)
            .addOnSuccessListener {
                if (notesList.value == null) {
                    notesList.value = emptyList()
                }
                dictionaryMeta.firestoreID = it.id
                notesList.value = notesList.value?.plus(dictionaryMeta)
                dbFetchDictionaryMeta(notesList)
            }

    }
}