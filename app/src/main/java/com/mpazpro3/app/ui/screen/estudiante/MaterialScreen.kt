package com.mpazpro3.app.ui.screen.estudiante

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.R
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel
import java.util.Locale

/**
 * Material de estudio del contenido elegido: explicación, imagen, audio de apoyo, ejemplo e
 * instrucciones. RF: "consultar el material asignado".
 *
 * TODO(equipo):
 *  - Mostrar la imagen real del recurso (hoy es un recuadro de ejemplo).
 *  - Reproducir el audio de apoyo (por ejemplo con MediaPlayer y un archivo en res/raw).
 *    RNF: el audio complementa la lectura, no la reemplaza.
 *  - confirmarMaterialRevisado() todavía no guarda el avance.
 */
@Composable
fun MaterialScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val context = LocalContext.current
    var tts by remember(context) { mutableStateOf<TextToSpeech?>(null) }
    var ttsInicializado by remember(context) { mutableStateOf(false) }
    var audioListo by remember(context) { mutableStateOf(false) }
    var audioError by remember(context) { mutableStateOf(false) }
    DisposableEffect(context) {
        val motor = TextToSpeech(context) { estado ->
            ttsInicializado = estado == TextToSpeech.SUCCESS
        }
        tts = motor
        onDispose {
            motor.stop()
            motor.shutdown()
            tts = null
            ttsInicializado = false
            audioListo = false
        }
    }
    LaunchedEffect(ttsInicializado) {
        if (ttsInicializado) {
            val motor = tts
            val chile = motor?.setLanguage(Locale("es", "CL"))
                ?: TextToSpeech.LANG_NOT_SUPPORTED
            val estadoIdioma = if (chile < TextToSpeech.LANG_AVAILABLE) {
                motor?.setLanguage(Locale("es", "ES")) ?: TextToSpeech.LANG_NOT_SUPPORTED
            } else chile
            audioListo = estadoIdioma >= TextToSpeech.LANG_AVAILABLE
            audioError = !audioListo
        }
    }

    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val contenido = viewModel.contenidoPorId(contenidoId)
    val recursos = viewModel.recursosDe(contenidoId)

    Scaffold(topBar = { BarraSuperior(contenido?.asignatura?.etiqueta ?: "Material", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (recursos.isEmpty()) {
                Text("Todavía no hay material para este contenido.", style = MaterialTheme.typography.bodyLarge)
            }

            recursos.forEach { recurso ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(recurso.titulo, style = MaterialTheme.typography.headlineSmall)
                        Text(recurso.explicacion, style = MaterialTheme.typography.bodyLarge)

                        if (recurso.descripcionImagen != null && recurso.contenidoId == "C2") {
                            Image(
                                painter = painterResource(R.drawable.bloques_base_diez),
                                contentDescription = "Cuatro centenas, cinco decenas y dos unidades representadas con bloques base diez",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                "4 centenas + 5 decenas + 2 unidades = 452",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (recurso.audioDisponible) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = audioListo,
                                onClick = {
                                    val texto = "${recurso.titulo}. ${recurso.explicacion}. Ejemplo: ${recurso.ejemplo}. Instrucciones: ${recurso.instrucciones}"
                                    val estado = tts?.speak(
                                        texto,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        recurso.id
                                    )
                                    if (estado == TextToSpeech.ERROR) audioError = true
                                }
                            ) {
                                Text(if (audioListo) "Escuchar audio de apoyo" else "Preparando audio...")
                            }
                            if (audioError) {
                                Text(
                                    "No hay una voz en español disponible en este dispositivo.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Text("Ejemplo", style = MaterialTheme.typography.titleMedium)
                        Text(recurso.ejemplo, style = MaterialTheme.typography.bodyLarge)
                        Text("Instrucciones", style = MaterialTheme.typography.titleMedium)
                        Text(recurso.instrucciones, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = {
                    val id = contenidoId
                    if (id != null) viewModel.confirmarMaterialRevisado(usuario?.id.orEmpty(), id)
                    navController.navigate(Rutas.ESTUDIANTE_DESAFIOS)
                }
            ) { Text("¡Listo! Ir a los desafíos", style = MaterialTheme.typography.titleMedium) }
        }
    }
}
