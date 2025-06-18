package com.example.travelapplicationv5

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseHandler {
    val supabaseUrl = "https://fbtjimufxercrkiiufxo.supabase.co"
    val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZidGppbXVmeGVyY3JraWl1ZnhvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkyNzA2ODksImV4cCI6MjA2NDg0NjY4OX0.qYS26CScEx5sXfM52Ncxpu2kPY-10Fi4ahSRmdMGH5k"
    val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    val bucketTrips = "content-viaggi"
    val nameTrips = "viaggi"

    val bucketReviews = "content-reviews"
    val nameReviews = "reviews"

    val bucketUsers = "content-users"
    val nameUsers = "users"

    suspend fun anonymousLogin() {
        val session = supabase.auth.currentSessionOrNull()
        if (session == null) {
            supabase.auth.signInAnonymously()
        }
    }

    suspend fun uploadImagesList(
        uris: List<String>,
        context: Context,
        bucketName: String
    ): List<String> {
        return uris.mapIndexed { index, uri ->
            val fileName = "uploads/$index${nameTrips}${System.currentTimeMillis()}.jpg"
            uploadImage(uri, context, fileName, bucketName)
        }.filterNotNull()
    }

    suspend fun uploadReviewImages(
        uris: List<String>,
        context: Context
    ): List<String> {
        return uris.mapIndexed { index, uri ->
            val fileName = "uploads/$index${nameReviews}${System.currentTimeMillis()}.jpg"
            uploadImage(uri, context, fileName, bucketReviews)
        }.filterNotNull()
    }

    suspend fun uploadUserImage(
        uriString: String,
        context: Context
    ): String? {
        val fileName = "uploads/profile_${System.currentTimeMillis()}.jpg"
        return uploadImage(uriString, context, fileName, bucketUsers)
    }

    suspend fun uploadImage(uriString: String, context: Context, fileName: String, bucketName: String): String? {
        if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
            return uriString
        }
        val uri = Uri.parse(uriString)

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()

        val storage = supabase.storage
        val bucket = storage[bucketName]

        try {
            bucket.upload(
                path = fileName,
                data = bytes
            )
        } catch (ex: Exception) {
            println("Error on uploading")
        }

        return bucket.publicUrl(fileName)
    }

    suspend fun deleteImages(imagePaths: List<String>, bucketName: String) {
        val bucket = supabase.storage[bucketName]

        try {
            bucket.delete(imagePaths)
        } catch (e: Exception) {
            println("Error deleting images: ${e.message}")
        }
    }
}