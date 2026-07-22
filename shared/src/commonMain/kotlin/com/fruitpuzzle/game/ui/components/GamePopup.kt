package com.fruitpuzzle.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modal popup for Win, Loss, and Game Over states.
 * Renders a semi-transparent overlay with a centered card.
 */
@Composable
fun GamePopup(
  title: String,
  message: String,
  primaryButtonText: String,
  onPrimaryClick: () -> Unit,
  secondaryButtonText: String? = null,
  onSecondaryClick: (() -> Unit)? = null
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.6f)),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 340.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(Color(0xFF1E1E3F))
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = message,
        fontSize = 16.sp,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(28.dp))

      Button(
        onClick = onPrimaryClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF4CAF50)
        )
      ) {
        Text(
          text = primaryButtonText,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      if (secondaryButtonText != null && onSecondaryClick != null) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onSecondaryClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          shape = RoundedCornerShape(22.dp)
        ) {
          Text(
            text = secondaryButtonText,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
          )
        }
      }
    }
  }
}
