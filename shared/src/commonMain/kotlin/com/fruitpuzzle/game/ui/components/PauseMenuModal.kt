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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
 * Interactive Pause & Settings modal.
 * Allows switching volume, muting music/SFX, adjusting UI scale, font scale, restarting, and returning to menu.
 */
@Composable
fun PauseMenuModal(
  isMuted: Boolean,
  bgmVolume: Float,
  sfxVolume: Float,
  uiScale: Float,
  fontScale: Float,
  onResume: () -> Unit,
  onMuteToggle: (Boolean) -> Unit,
  onBgmVolumeChange: (Float) -> Unit,
  onSfxVolumeChange: (Float) -> Unit,
  onUiScaleChange: (Float) -> Unit,
  onFontScaleChange: (Float) -> Unit,
  onRestart: () -> Unit,
  onBackToMenu: () -> Unit
) {
  val baseFontSize = 16.sp * fontScale

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.75f)),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 380.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(Color(0xFF1E1E38))
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "⏸️  Jogo Pausado",
        fontSize = 24.sp * fontScale,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Mute All Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "🔇 Mute Geral",
          fontSize = baseFontSize,
          color = Color.White
        )
        Switch(
          checked = isMuted,
          onCheckedChange = onMuteToggle,
          colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF5252))
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // BGM Volume Slider
      Text(
        text = "🎵 Música de Fundo: ${(bgmVolume * 100).toInt()}%",
        fontSize = baseFontSize * 0.9f,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
      )
      Slider(
        value = bgmVolume,
        onValueChange = onBgmVolumeChange,
        enabled = !isMuted,
        colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF81C784))
      )

      // SFX Volume Slider
      Text(
        text = "🔊 Efeitos Sonoros: ${(sfxVolume * 100).toInt()}%",
        fontSize = baseFontSize * 0.9f,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
      )
      Slider(
        value = sfxVolume,
        onValueChange = onSfxVolumeChange,
        enabled = !isMuted,
        colors = SliderDefaults.colors(thumbColor = Color(0xFF2196F3), activeTrackColor = Color(0xFF64B5F6))
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Interface Scale Selector
      Text(
        text = "📐 Tamanho da Interface",
        fontSize = baseFontSize * 0.9f,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        ScaleChip(label = "80%", isSelected = uiScale == 0.8f, onClick = { onUiScaleChange(0.8f) })
        ScaleChip(label = "100%", isSelected = uiScale == 1.0f, onClick = { onUiScaleChange(1.0f) })
        ScaleChip(label = "120%", isSelected = uiScale == 1.2f, onClick = { onUiScaleChange(1.2f) })
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Font Scale Selector
      Text(
        text = "🔤 Tamanho da Fonte",
        fontSize = baseFontSize * 0.9f,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        ScaleChip(label = "Pequeno", isSelected = fontScale == 0.85f, onClick = { onFontScaleChange(0.85f) })
        ScaleChip(label = "Médio", isSelected = fontScale == 1.0f, onClick = { onFontScaleChange(1.0f) })
        ScaleChip(label = "Grande", isSelected = fontScale == 1.15f, onClick = { onFontScaleChange(1.15f) })
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Resume Button
      Button(
        onClick = onResume,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
      ) {
        Text(text = "▶  Continuar Jogo", fontSize = baseFontSize, fontWeight = FontWeight.Bold, color = Color.White)
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onRestart,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(20.dp)
        ) {
          Text(text = "🔄 Reiniciar", fontSize = baseFontSize * 0.85f, color = Color.White)
        }

        OutlinedButton(
          onClick = onBackToMenu,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(20.dp)
        ) {
          Text(text = "🏠 Voltar ao Menu", fontSize = baseFontSize * 0.85f, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun ScaleChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isSelected) Color(0xFF3F51B5) else Color.White.copy(alpha = 0.15f)
    )
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = Color.White
    )
  }
}
