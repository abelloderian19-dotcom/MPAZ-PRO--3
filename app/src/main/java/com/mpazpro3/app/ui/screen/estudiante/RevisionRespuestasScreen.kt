package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RevisionRespuestasScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val respuestas = viewModel.respuestasDe(usuario?.id.orEmpty(), contenidoId.orEmpty())
    val intentosDesafios = respuestas.filter {
        viewModel.actividadPorId(it.actividadId)?.esEvaluacionFinal != true
    }.groupBy { respuesta -> "${respuesta.actividadId} · intento ${respuesta.numeroIntento}" }
        .toList().sortedBy { (_, grupo) -> grupo.minOf { it.fechaHora } }
    val intentosEvaluacion = respuestas.filter {
        viewModel.actividadPorId(it.actividadId)?.esEvaluacionFinal == true
    }.groupBy { respuesta -> "Intento ${respuesta.numeroIntento}" }
        .toList().sortedBy { (_, grupo) -> grupo.minOf { it.fechaHora } }
    var desafiosAbiertos by remember(contenidoId, usuario?.id) { mutableStateOf(true) }
    var evaluacionAbierta by remember(contenidoId, usuario?.id) { mutableStateOf(true) }

    Scaffold(topBar = { BarraSuperior("Mis respuestas", navController) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (respuestas.isEmpty()) {
                Text("Todavía no hay respuestas para revisar.", style = MaterialTheme.typography.bodyLarge)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { desafiosAbiertos = !desafiosAbiertos }
                    ) { Text("Desafíos (${intentosDesafios.size}) ${if (desafiosAbiertos) "▲" else "▼"}") }
                }
                if (desafiosAbiertos) {
                    items(intentosDesafios) { (titulo, grupo) ->
                        IntentoRespuestaCard(titulo, grupo, viewModel)
                    }
                }
                item {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { evaluacionAbierta = !evaluacionAbierta }
                    ) { Text("Evaluación final (${intentosEvaluacion.size}) ${if (evaluacionAbierta) "▲" else "▼"}") }
                }
                if (evaluacionAbierta) {
                    items(intentosEvaluacion) { (titulo, grupo) ->
                        IntentoRespuestaCard("Evaluación final · $titulo", grupo, viewModel)
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = {
                    navController.navigate(Rutas.ESTUDIANTE_INICIO) {
                        popUpTo(Rutas.ESTUDIANTE_INICIO) { inclusive = true }
                    }
                }
            ) { Text("Volver a mis actividades", style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
private fun IntentoRespuestaCard(
    titulo: String,
    respuestas: List<RespuestaEstudiante>,
    viewModel: MpazProViewModel
) {
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(respuestas.minOf { it.fechaHora }))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text("Fecha y hora: $fecha", style = MaterialTheme.typography.bodyMedium)
            respuestas.forEach { respuesta ->
                val actividad = viewModel.actividadPorId(respuesta.actividadId)
                val elegida = actividad?.alternativas?.getOrNull(respuesta.alternativaElegida)?.texto.orEmpty()
                Text(actividad?.enunciado.orEmpty(), style = MaterialTheme.typography.titleSmall)
                Text("Tu respuesta: $elegida", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (respuesta.esCorrecta) "Correcta" else "Incorrecta",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (respuesta.esCorrecta) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
