package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

@Composable
fun DesafiosScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val desafios = remember(contenidoId) { viewModel.desafiosDe(contenidoId) }
    var indice by remember(contenidoId) { mutableIntStateOf(0) }
    var confirmarReintentoDesafios by remember { mutableStateOf(false) }

    val respuestasGuardadas = remember(contenidoId, usuario?.id) {
        viewModel.respuestasDe(usuario?.id.orEmpty(), contenidoId.orEmpty())
    }
    var alternativasSeleccionadas by remember(contenidoId, usuario?.id) {
        mutableStateOf(respuestasGuardadas.associate { it.actividadId to it.alternativaElegida })
    }
    var resultados by remember(contenidoId, usuario?.id) {
        mutableStateOf(respuestasGuardadas.associate { it.actividadId to it.esCorrecta })
    }
    var desafiosRespondidos by remember(contenidoId, usuario?.id) {
        mutableStateOf(respuestasGuardadas.map { it.actividadId }.toSet())
    }

    fun comprobarDesafio(actividad: Actividad) {
        val alternativa = alternativasSeleccionadas[actividad.id] ?: return
        if (actividad.id in desafiosRespondidos) return
        val resultado = viewModel.responderDesafio(usuario?.id.orEmpty(), actividad, alternativa)
        if (resultado != null) {
            resultados = resultados + (actividad.id to resultado)
            desafiosRespondidos = desafiosRespondidos + actividad.id
        }
    }

    Scaffold(topBar = { BarraSuperior("Desafíos", navController) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (desafios.isEmpty()) {
                Text("No hay desafíos para este contenido.", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            val actual = desafios[indice.coerceIn(0, desafios.lastIndex)]
            Text("Desafío ${indice + 1} de ${desafios.size} · ${actual.dificultad.etiqueta}", style = MaterialTheme.typography.labelLarge)
            Text(actual.enunciado, style = MaterialTheme.typography.headlineSmall)

            actual.alternativas.forEachIndexed { posicion, alternativa ->
                val seleccionada = alternativasSeleccionadas[actual.id] == posicion
                val alSeleccionar = { alternativasSeleccionadas = alternativasSeleccionadas + (actual.id to posicion) }
                if (seleccionada) {
                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = actual.id !in desafiosRespondidos,
                        onClick = alSeleccionar
                    ) { Text(alternativa.texto, style = MaterialTheme.typography.titleMedium) }
                } else {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = actual.id !in desafiosRespondidos,
                        onClick = alSeleccionar
                    ) { Text(alternativa.texto, style = MaterialTheme.typography.titleMedium) }
                }
            }

            if (actual.id !in desafiosRespondidos) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = alternativasSeleccionadas.containsKey(actual.id),
                    onClick = { comprobarDesafio(actual) }
                ) { Text("Comprobar respuesta") }
            } else {
                Text(
                    when (resultados[actual.id]) {
                        true -> "¡Correcto!"
                        false -> "Inténtalo de nuevo."
                        null -> "No se pudo registrar la respuesta."
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = indice > 0,
                    onClick = { indice-- }
                ) { Text("Anterior") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = indice < desafios.lastIndex,
                    onClick = {
                        comprobarDesafio(actual)
                        indice++
                    }
                ) { Text("Siguiente") }
            }

            if (desafios.all { it.id in desafiosRespondidos }) {
                val correctos = desafios.count { resultados[it.id] == true }
                Text("Resultados de práctica: $correctos de ${desafios.size} correctos.", style = MaterialTheme.typography.titleMedium)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { confirmarReintentoDesafios = true }
                ) { Text("Reintentar todos los desafíos") }
            }

            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = { navController.navigate(Rutas.ESTUDIANTE_EVALUACION) }
            ) { Text("Ir a la evaluación final", style = MaterialTheme.typography.titleMedium) }
        }
    }

    if (confirmarReintentoDesafios) {
        AlertDialog(
            onDismissRequest = { confirmarReintentoDesafios = false },
            title = { Text("¿Reintentar todos los desafíos?") },
            text = { Text("Se reiniciarán todas las preguntas para comenzar un nuevo intento del desafío.") },
            confirmButton = {
                Button(onClick = {
                    desafiosRespondidos = emptySet()
                    alternativasSeleccionadas = emptyMap()
                    resultados = emptyMap()
                    indice = 0
                    confirmarReintentoDesafios = false
                }) { Text("Sí, reintentar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmarReintentoDesafios = false }) { Text("Cancelar") }
            }
        )
    }
}
