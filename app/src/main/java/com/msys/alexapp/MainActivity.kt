package com.msys.alexapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.msys.alexapp.components.NavComposable
import com.msys.alexapp.services.FirebaseService
import com.msys.alexapp.ui.theme.AlexAppTheme

class MainActivity : ComponentActivity() {
  init {
    System.setProperty(
      "org.apache.poi.javax.xml.stream.XMLInputFactory",
      "com.fasterxml.aalto.stax.InputFactoryImpl"
    )
    System.setProperty(
      "org.apache.poi.javax.xml.stream.XMLOutputFactory",
      "com.fasterxml.aalto.stax.OutputFactoryImpl"
    )
    System.setProperty(
      "org.apache.poi.javax.xml.stream.XMLEventFactory",
      "com.fasterxml.aalto.stax.EventFactoryImpl"
    )
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AlexAppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          FirebaseService.NavComposable()
        }
      }
    }
  }
}