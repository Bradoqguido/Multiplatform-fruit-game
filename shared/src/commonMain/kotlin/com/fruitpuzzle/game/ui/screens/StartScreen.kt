package com.fruitpuzzle.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fruitpuzzle.game.platform.exitApp
import com.fruitpuzzle.game.platform.showExitButton

/**
 * Start screen — minimalist landing page with gradient background,
 * game title, "Start" button, and conditional "Exit" button (Desktop only).
 */
@Composable
fun StartScreen(
  onStartClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF1A237E),
            Color(0xFF4A148C),
            Color(0xFF880E4F)
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 400.dp)
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Emoji header
      Text(
        text = "🍎🍊🍋🍇🍓",
        fontSize = 48.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Title
      Text(
        text = "Jogo das Frutas",
        fontSize = 42.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Trinca de Frutas",
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFFFCC80),
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(48.dp))

      // Start button
      Button(
        onClick = onStartClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4CAF50)
        ),
        shape = RoundedCornerShape(28.dp)
      ) {
        Text(
          text = "▶  Iniciar Jogo",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      // Exit button
      if (showExitButton) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
          onClick = { exitApp() },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
          shape = RoundedCornerShape(28.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
          )
        ) {
          Text(
            text = "❌  Sair do Jogo",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f)
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      Text(
        text = "100% Offline • Sem Anúncios",
        fontSize = 14.sp,
        color = Color.White.copy(alpha = 0.5f),
        textAlign = TextAlign.Center
      )
    }
  }
}
