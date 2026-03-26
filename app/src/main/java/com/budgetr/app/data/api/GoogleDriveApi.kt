package com.budgetr.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDriveApi {

    @GET("files")
    suspend fun listFiles(
        @Query("q") query: String = "mimeType='application/vnd.google-apps.spreadsheet' and trashed=false",
        @Query("fields") fields: String = "files(id,name)",
        @Query("orderBy") orderBy: String = "name"
    ): DriveFileList
}

@JsonClass(generateAdapter = true)
data class DriveFileList(
    @Json(name = "files") val files: List<DriveFile>? = null
)

@JsonClass(generateAdapter = true)
data class DriveFile(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String
)
