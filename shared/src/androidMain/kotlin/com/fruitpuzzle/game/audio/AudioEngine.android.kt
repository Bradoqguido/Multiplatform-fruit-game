package com.fruitpuzzle.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

actual object AudioEngine {
  private val scope = CoroutineScope(Dispatchers.Default)
  private var bgmJob: Job? = null
  private var bgmVolumeLevel = 0.5f
  private var bgmMuted = false

  // Pre-synthesized PCM buffers for instant playback
  private val clickBuffer: ByteArray by lazy { generateSineSweep(600f, 1000f, 0.06f) }
  private val dropBuffer: ByteArray by lazy { generateSineSweep(400f, 200f, 0.08f) }
  private val matchBuffer: ByteArray by lazy { generateChime(listOf(523.25f, 659.25f, 783.99f), 0.25f) }

  actual fun playClickSfx(volume: Float, isMuted: Boolean) {
    if (isMuted || volume <= 0f) return
    scope.launch { playPcm(clickBuffer, volume) }
  }

  actual fun playDropSfx(volume: Float, isMuted: Boolean) {
    if (isMuted || volume <= 0f) return
    scope.launch { playPcm(dropBuffer, volume) }
  }

  actual fun playMatchSfx(volume: Float, isMuted: Boolean) {
    if (isMuted || volume <= 0f) return
    scope.launch { playPcm(matchBuffer, volume) }
  }

  actual fun startBgMusic(volume: Float, isMuted: Boolean) {
    bgmVolumeLevel = volume
    bgmMuted = isMuted
    if (bgmJob?.isActive == true) return

    bgmJob = scope.launch {
      val notes = listOf(261.63f, 329.63f, 392.00f, 523.25f, 392.00f, 329.63f)
      var noteIndex = 0

      while (isActive) {
        if (!bgmMuted && bgmVolumeLevel > 0f) {
          val freq = notes[noteIndex % notes.size]
          val pcm = generateSineTone(freq, 0.35f)
          playPcm(pcm, bgmVolumeLevel * 0.25f)
        }
        noteIndex++
        delay(600)
      }
    }
  }

  actual fun stopBgMusic() {
    bgmJob?.cancel()
    bgmJob = null
  }

  actual fun updateBgmVolume(volume: Float, isMuted: Boolean) {
    bgmVolumeLevel = volume
    bgmMuted = isMuted
    if (!isMuted && volume > 0f && bgmJob?.isActive != true) {
      startBgMusic(volume, isMuted)
    }
  }

  private fun playPcm(pcm: ByteArray, volume: Float) {
    try {
      val sampleRate = 22050
      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(pcm.size)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(pcm, 0, pcm.size)
      track.setVolume(volume.coerceIn(0f, 1f))
      track.play()
      // Release after playback completes
      scope.launch {
        delay(350)
        track.release()
      }
    } catch (_: Exception) {
      // Audio fallback safety
    }
  }

  private fun generateSineTone(freq: Float, durationSec: Float, sampleRate: Int = 22050): ByteArray {
    val numSamples = (durationSec * sampleRate).toInt()
    val buffer = ByteArray(numSamples * 2)
    val twopi = 2.0 * Math.PI

    for (i in 0 until numSamples) {
      val t = i.toDouble() / sampleRate
      // Apply smooth envelope to eliminate clicks
      val envelope = sin(Math.PI * i / numSamples)
      val sample = (sin(twopi * freq * t) * 16000 * envelope).toInt().toShort()

      buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
      buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return buffer
  }

  private fun generateSineSweep(startFreq: Float, endFreq: Float, durationSec: Float, sampleRate: Int = 22050): ByteArray {
    val numSamples = (durationSec * sampleRate).toInt()
    val buffer = ByteArray(numSamples * 2)
    val twopi = 2.0 * Math.PI

    for (i in 0 until numSamples) {
      val fraction = i.toDouble() / numSamples
      val freq = startFreq + (endFreq - startFreq) * fraction
      val t = i.toDouble() / sampleRate
      val envelope = sin(Math.PI * fraction)
      val sample = (sin(twopi * freq * t) * 18000 * envelope).toInt().toShort()

      buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
      buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return buffer
  }

  private fun generateChime(freqs: List<Float>, durationSec: Float, sampleRate: Int = 22050): ByteArray {
    val numSamples = (durationSec * sampleRate).toInt()
    val buffer = ByteArray(numSamples * 2)
    val twopi = 2.0 * Math.PI

    for (i in 0 until numSamples) {
      val t = i.toDouble() / sampleRate
      val envelope = sin(Math.PI * i / numSamples)
      var sum = 0.0
      for (f in freqs) {
        sum += sin(twopi * f * t)
      }
      val sample = (sum / freqs.size * 18000 * envelope).toInt().toShort()

      buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
      buffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
    }
    return buffer
  }
}
