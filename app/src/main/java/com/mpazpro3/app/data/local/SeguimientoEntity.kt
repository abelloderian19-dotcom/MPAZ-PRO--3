package com.mpazpro3.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance

@Entity(tableName = "seguimientos", primaryKeys = ["estudianteId", "contenidoId"])
data class SeguimientoEntity(
    val estudianteId: String,
    val contenidoId: String,
    val intentosEvaluacionUsados: Int,
    val estado: String
)

@Entity(
    tableName = "respuestas",
    primaryKeys = ["estudianteId", "contenidoId", "actividadId", "numeroIntento"],
    foreignKeys = [ForeignKey(
        entity = SeguimientoEntity::class,
        parentColumns = ["estudianteId", "contenidoId"],
        childColumns = ["estudianteId", "contenidoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("estudianteId", "contenidoId")]
)
data class RespuestaEntity(
    val estudianteId: String,
    val contenidoId: String,
    val actividadId: String,
    val alternativaElegida: Int,
    val esCorrecta: Boolean,
    val numeroIntento: Int,
    val fechaHora: Long
)

fun SeguimientoAvance.toEntities(): Pair<SeguimientoEntity, List<RespuestaEntity>> =
    SeguimientoEntity(estudianteId, contenidoId, intentosEvaluacionUsados, estado.name) to
        respuestas.map { respuesta ->
            RespuestaEntity(
                estudianteId, contenidoId, respuesta.actividadId, respuesta.alternativaElegida,
                respuesta.esCorrecta, respuesta.numeroIntento, respuesta.fechaHora
            )
        }

fun SeguimientoEntity.toModel(respuestas: List<RespuestaEntity>): SeguimientoAvance = SeguimientoAvance(
    estudianteId = estudianteId,
    contenidoId = contenidoId,
    respuestas = respuestas.map {
        RespuestaEstudiante(it.actividadId, it.alternativaElegida, it.esCorrecta, it.numeroIntento, it.fechaHora)
    },
    intentosEvaluacionUsados = intentosEvaluacionUsados,
    estado = runCatching { EstadoAvance.valueOf(estado) }.getOrDefault(EstadoAvance.NO_INICIADO)
)
