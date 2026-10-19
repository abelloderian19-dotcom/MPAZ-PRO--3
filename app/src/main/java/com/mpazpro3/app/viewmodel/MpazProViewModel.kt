package com.mpazpro3.app.viewmodel

import androidx.lifecycle.ViewModel
import com.mpazpro3.app.data.MockData
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * ESTO ES UN ESQUELETO: las pantallas ya llaman a todas estas funciones, pero la lógica de
 * negocio del caso está vacía a propósito y marcada con TODO(equipo), citando el módulo de
 * Requerimientos_Seccion007 al que corresponde. Mientras no se implemente, las listas se
 * muestran sin filtrar y las acciones (aprobar, asignar, responder, validar) no cambian nada.
 */
class MpazProViewModel : ViewModel() {

    // =====================================================================
    // Autenticación
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * TODO(equipo): validar usuario y clave contra MockData.usuarios. Si son correctos, dejar
     * _usuarioActivo con ese usuario y devolver true (AppNavigation ya lleva a cada perfil a su
     * pantalla). Si no, dejar _errorLogin con un mensaje y devolver false.
     * Pendiente con el CITT: cómo se autentica el estudiante.
     */
    fun login(usuario: String, clave: String): Boolean {
        _errorLogin.value = "Login sin implementar — ver TODO(equipo) en MpazProViewModel.login()."
        return false
    }

    /**
     * SOLO PARA DESARROLLO: entra con el usuario de prueba del perfil indicado, sin validar
     * credenciales. TODO(equipo): eliminar esta función y los botones "Modo desarrollo".
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first { it.perfil == perfil }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _contenidoSeleccionadoId.value = null
        _estudianteSeleccionadoId.value = null
    }

    // =====================================================================
    // Datos compartidos y selección (no es lógica de negocio)
    // =====================================================================

    private val _contenidos = MutableStateFlow(MockData.contenidos)
    val contenidos: StateFlow<List<Contenido>> = _contenidos.asStateFlow()

    private val _recursos = MutableStateFlow(MockData.recursos)
    private val _actividades = MutableStateFlow(MockData.actividades)

    private val _asignaciones = MutableStateFlow(MockData.asignaciones)
    val asignaciones: StateFlow<List<Asignacion>> = _asignaciones.asStateFlow()

    private val _estudiantes = MutableStateFlow(MockData.estudiantes)
    val estudiantes: StateFlow<List<Estudiante>> = _estudiantes.asStateFlow()

    private val _seguimientos = MutableStateFlow(MockData.seguimientos)
    val seguimientos: StateFlow<List<SeguimientoAvance>> = _seguimientos.asStateFlow()

    private val _informes = MutableStateFlow<List<InformeDocente>>(emptyList())
    val informes: StateFlow<List<InformeDocente>> = _informes.asStateFlow()

    private val _contenidoSeleccionadoId = MutableStateFlow<String?>(null)
    val contenidoSeleccionadoId: StateFlow<String?> = _contenidoSeleccionadoId.asStateFlow()

    private val _estudianteSeleccionadoId = MutableStateFlow<String?>(null)
    val estudianteSeleccionadoId: StateFlow<String?> = _estudianteSeleccionadoId.asStateFlow()

    fun seleccionarContenido(id: String?) {
        _contenidoSeleccionadoId.value = id
    }

    fun seleccionarEstudiante(id: String?) {
        _estudianteSeleccionadoId.value = id
    }

    fun contenidoPorId(id: String?): Contenido? = _contenidos.value.find { it.id == id }

    fun estudiantePorId(id: String?): Estudiante? = _estudiantes.value.find { it.id == id }

    fun recursosDe(contenidoId: String?): List<RecursoDidactico> =
        _recursos.value.filter { it.contenidoId == contenidoId }

    fun actividadPorId(id: String): Actividad? = _actividades.value.find { it.id == id }

    // =====================================================================
    // Módulo "Autorización de contenidos (UTP)"
    // =====================================================================

    /**
     * TODO(equipo): filtrar por asignatura y por estado (null = todos). Hoy devuelve todo.
     */
    fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): List<Contenido> = _contenidos.value

    /**
     * TODO(equipo): cambiar el estado del contenido a APROBADO y guardar la observación.
     * RF: "UTP debe poder revisar y autorizar/rechazar un contenido" y "registrar el estado de
     * aprobación de cada contenido". Hoy no hace nada.
     */
    fun aprobarContenido(contenidoId: String, observacion: String) {
        // Sin implementar todavía.
    }

    /** TODO(equipo): igual que aprobarContenido(), pero con estado RECHAZADO. Hoy no hace nada. */
    fun rechazarContenido(contenidoId: String, observacion: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Planificación docente"
    // =====================================================================

    /** Devuelve únicamente contenidos autorizados por UTP. */
    fun contenidosDisponiblesParaAsignar(): List<Contenido> =
        _contenidos.value.filter { it.estado == EstadoAprobacion.APROBADO }

    /** Crea o actualiza asignaciones, rechazando el lote si contiene OA no autorizados. */
    fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int): Boolean {
        if (maxIntentos < 1 || curso.isBlank() || contenidoIds.isEmpty()) return false
        val solicitados = contenidoIds.toSet()
        val aprobados = _contenidos.value
            .filter { it.id in solicitados && it.estado == EstadoAprobacion.APROBADO }
            .map { it.id }
            .toSet()
        if (aprobados != solicitados) return false

        val actualizadas = _asignaciones.value.toMutableList()
        var siguiente = (actualizadas.mapNotNull { it.id.removePrefix("AS").toIntOrNull() }.maxOrNull() ?: 0) + 1
        aprobados.forEach { contenidoId ->
            val indice = actualizadas.indexOfFirst { it.curso == curso && it.contenidoId == contenidoId }
            if (indice >= 0) {
                actualizadas[indice] = actualizadas[indice].copy(maxIntentosEvaluacion = maxIntentos)
            } else {
                actualizadas.add(Asignacion("AS${siguiente++}", curso, contenidoId, maxIntentos))
            }
        }
        _asignaciones.value = actualizadas
        return true
    }

    /** TODO(equipo): devolver solo las asignaciones del curso indicado. Hoy devuelve todas. */
    fun asignacionesDelCurso(curso: String): List<Asignacion> = _asignaciones.value

    /**
     * TODO(equipo): calcular el avance del estudiante (0.0 a 1.0) a partir de sus
     * SeguimientoAvance. RF: "revisar los resultados de sus estudiantes". Hoy devuelve 0.
     */
    fun progresoGeneral(estudianteId: String): Float = 0f

    /**
     * TODO(equipo): exportar los resultados del curso (por ejemplo a un archivo CSV o PDF que
     * se pueda compartir). RF: "revisar, descargar y validar los resultados". Hoy no hace nada.
     */
    fun descargarResultados(curso: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Informe pedagógico"
    // =====================================================================

    fun informeDe(estudianteId: String?): InformeDocente? = _informes.value.find { it.estudianteId == estudianteId }

    /**
     * TODO(equipo): generar el informe del estudiante (rendimiento, fortalezas y aspectos a
     * reforzar) a partir de sus respuestas, en estado BORRADOR. Restricciones del caso: sin
     * diagnósticos ni consecuencias académicas automáticas. Hoy no hace nada.
     */
    fun generarInforme(estudianteId: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): pasar el informe de BORRADOR a VALIDADO. RF: "el informe debe quedar
     * sujeto a validación docente antes de considerarse definitivo". Hoy no hace nada.
     */
    fun validarInforme(informeId: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Experiencia del estudiante"
    // =====================================================================

    /** Devuelve las asignaturas/contenidos disponibles para presentar en el inicio del estudiante. */
    fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> = _contenidos.value

    /** TODO(equipo): avance del estudiante en el contenido (0.0 a 1.0). Hoy devuelve 0. */
    fun progreso(estudianteId: String, contenidoId: String): Float = 0f

    /**
     * TODO(equipo): guardar que el estudiante terminó de revisar el material. RF: "conservar el
     * progreso del estudiante y confirmar la finalización de cada actividad". Hoy no hace nada.
     */
    fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String) {
        // Sin implementar todavía.
    }

    fun desafiosDe(contenidoId: String?): List<Actividad> {
        if (contenidoId.isNullOrBlank()) return emptyList()
        return _actividades.value
            .filter { it.contenidoId == contenidoId && !it.esEvaluacionFinal }
            .sortedWith(compareBy<Actividad> { it.dificultad.ordinal }.thenBy { it.orden })
    }

    fun responderDesafio(estudianteId: String, actividad: Actividad, alternativa: Int): Boolean? {
        if (estudianteId.isBlank() || actividad.esEvaluacionFinal) return null
        val registrada = _actividades.value.firstOrNull {
            it.id == actividad.id && it.contenidoId == actividad.contenidoId && !it.esEvaluacionFinal
        } ?: return null
        if (alternativa !in registrada.alternativas.indices) return null
        val correcta = registrada.alternativas[alternativa].esCorrecta
        val seguimientos = _seguimientos.value
        val indice = seguimientos.indexOfFirst {
            it.estudianteId == estudianteId && it.contenidoId == registrada.contenidoId
        }
        val seguimiento = seguimientos.getOrNull(indice)
            ?: SeguimientoAvance(estudianteId, registrada.contenidoId)
        val intento = (seguimiento.respuestas.filter { it.actividadId == registrada.id }
            .maxOfOrNull { it.numeroIntento } ?: 0) + 1
        val respuesta = RespuestaEstudiante(registrada.id, alternativa, correcta, intento)
        val actualizado = seguimiento.copy(
            respuestas = seguimiento.respuestas + respuesta,
            estado = EstadoAvance.EN_PROGRESO
        )
        _seguimientos.value = if (indice >= 0) {
            seguimientos.toMutableList().also { it[indice] = actualizado }
        } else seguimientos + actualizado
        return correcta
    }

    fun evaluacionFinalDe(contenidoId: String?): List<Actividad> =
        if (contenidoId.isNullOrBlank()) emptyList()
        else _actividades.value.filter { it.contenidoId == contenidoId && it.esEvaluacionFinal }
            .sortedBy { it.orden }

    fun intentosRestantes(estudianteId: String, contenidoId: String): Int? {
        val estudiante = _estudiantes.value.firstOrNull { it.id == estudianteId } ?: return null
        val maximo = _asignaciones.value
            .filter { it.curso == estudiante.curso && it.contenidoId == contenidoId }
            .maxOfOrNull { it.maxIntentosEvaluacion } ?: return null
        val usados = _seguimientos.value
            .firstOrNull { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
            ?.intentosEvaluacionUsados ?: 0
        return (maximo - usados).coerceAtLeast(0)
    }

    fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>): Boolean {
        if (intentosRestantes(estudianteId, contenidoId)?.let { it > 0 } != true) return false
        val preguntas = evaluacionFinalDe(contenidoId)
        if (preguntas.isEmpty() || respuestas.keys != preguntas.map { it.id }.toSet()) return false
        if (preguntas.any { p -> respuestas[p.id]?.let { it in p.alternativas.indices } != true }) return false
        val existentes = _seguimientos.value
        val indice = existentes.indexOfFirst { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
        val seguimiento = existentes.getOrNull(indice) ?: SeguimientoAvance(estudianteId, contenidoId)
        val intento = seguimiento.intentosEvaluacionUsados + 1
        val fechaHoraEnvio = System.currentTimeMillis()
        val nuevasRespuestas = preguntas.map { p ->
            val elegida = respuestas.getValue(p.id)
            RespuestaEstudiante(p.id, elegida, p.alternativas[elegida].esCorrecta, intento, fechaHoraEnvio)
        }
        val actualizado = seguimiento.copy(
            respuestas = seguimiento.respuestas + nuevasRespuestas,
            intentosEvaluacionUsados = intento,
            estado = EstadoAvance.FINALIZADO
        )
        _seguimientos.value = if (indice >= 0) {
            existentes.toMutableList().also { it[indice] = actualizado }
        } else existentes + actualizado
        return true
    }

    fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> =
        _seguimientos.value.firstOrNull {
            it.estudianteId == estudianteId && it.contenidoId == contenidoId
        }?.respuestas.orEmpty()
}
