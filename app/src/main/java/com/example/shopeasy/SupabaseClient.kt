package com.example.shopeasy

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp


object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://octwtjovandhczqazvgr.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9jdHd0am92YW5kaGN6cWF6dmdyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg3MTYyMTMsImV4cCI6MjA5NDI5MjIxM30.NEPBMHUvJuc-o5cD7hz-PMYkItHF7-lXT3l9KdW7oFU"
    ){
        httpEngine = OkHttp.create()
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}