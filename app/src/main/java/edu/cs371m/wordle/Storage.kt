package edu.cs371m.wordle

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

class Storage {
    private val dictionaryStorage: StorageReference =
        Firebase.storage.reference.child("dictionaries")

    fun uploadDictionary(uri: Uri, uuid: String, uploadSuccess:(Long)->Unit) {
        val uploadTask = dictionaryStorage.child("$uuid").putFile(uri)
        uploadTask
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Upload FAILED $uuid")
            }
            .addOnSuccessListener {
                val sizeBytes = it.metadata?.sizeBytes ?: -1
                uploadSuccess(sizeBytes)
                Log.d(javaClass.simpleName, "Upload succeeded $uuid")
            }
    }

    fun getWords(uuid: String, wordLength: Int, words: MutableLiveData<List<String>>) {
        val localFile = File.createTempFile("dictionaries", "txt")
        dictionaryStorage.child(uuid).getFile(localFile).addOnSuccessListener {
            words.value = localFile.readLines().filter { word -> word.length == wordLength }
        }.addOnFailureListener {
            Log.d(javaClass.simpleName, "could not create temp file $localFile")
        }
    }

}