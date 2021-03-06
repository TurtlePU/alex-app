package com.example.alexapp.components

import GetQueue
import Performance
import PostAuth
import PostGrade
import Protocol
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

class NetworkState : ViewModel() {
  lateinit var showError: suspend (String) -> Unit
  private val snapshot: AtomicReference<Snapshot> by mutableStateOf(AtomicReference())

  private val client: HttpClient = HttpClient(CIO) {
    install(JsonFeature) {
      serializer = KotlinxSerializer(Protocol.json)
    }
  }

  data class Snapshot(val host: String, val login: String, val token: String)

  suspend fun auth(snapshot: Snapshot): Boolean {
    return try {
      val response: HttpResponse = client.post("${snapshot.host}/auth") {
        contentType(ContentType.Application.Json)
        body = PostAuth(snapshot.login, snapshot.token)
      }
      assert(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created)
      this.snapshot.set(snapshot)
      true
    } catch (e: Throwable) {
      Log.e(null, e.toString())
      showError(e.localizedMessage ?: "AUTH: Unknown error")
      false
    }
  }

  suspend fun refresh(since: Int): Sequence<Performance> {
    return try {
      val (host, _, _) = snapshot.get()
      withContext(viewModelScope.coroutineContext) {
        client.get<Array<Performance>>("$host/queue") {
          contentType(ContentType.Application.Json)
          body = GetQueue(since)
        }.asSequence()
      }
    } catch (e: Throwable) {
      Log.e(null, e.toString())
      showError(e.localizedMessage ?: "REFRESH: Unknown error")
      emptySequence()
    }
  }

  suspend fun grade(performance: Performance, grade: Double, comment: String?) {
    return try {
      val (host, jury, token) = snapshot.get()
      val response: HttpResponse = client.post("$host/grade") {
        contentType(ContentType.Application.Json)
        body = PostGrade(jury, token, performance, grade, comment)
      }
      assert(response.status == HttpStatusCode.OK)
    } catch (e: Throwable) {
      Log.e(null, e.toString())
      showError(e.localizedMessage ?: "GRADE: Unknown error")
    }
  }

  fun closeClient() {
    client.close()
  }
}