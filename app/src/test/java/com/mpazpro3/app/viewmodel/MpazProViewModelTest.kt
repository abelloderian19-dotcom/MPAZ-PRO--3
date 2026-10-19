package com.mpazpro3.app.viewmodel

import com.mpazpro3.app.model.EstadoAvance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpazProViewModelTest {

    @Test
    fun contenidosDisponiblesIncluyeSoloLosAprobados() {
        val viewModel = MpazProViewModel()

        assertEquals(listOf("C2"), viewModel.contenidosDisponiblesParaAsignar().map { it.id })
    }

    @Test
    fun asignacionRechazaMaximoInvalidoYActualizaIntentos() {
        val viewModel = MpazProViewModel()
        val asignacionOriginal = viewModel.asignaciones.value.single()

        assertFalse(viewModel.asignarAlCurso("3° básico A", listOf("C2"), 0))
        assertEquals(asignacionOriginal, viewModel.asignaciones.value.single())

        assertTrue(viewModel.asignarAlCurso("3° básico A", listOf("C2"), 1))
        assertEquals(1, viewModel.asignaciones.value.single().maxIntentosEvaluacion)
        assertFalse(viewModel.asignarAlCurso("3° básico A", listOf("C1", "C2"), 3))
        assertEquals(1, viewModel.asignaciones.value.single().maxIntentosEvaluacion)
    }

    @Test
    fun desafiosSeFiltranOrdenanYRegistranReintentos() {
        val viewModel = MpazProViewModel()
        val desafios = viewModel.desafiosDe("C2")

        assertEquals(listOf("A1", "A2", "A3"), desafios.map { it.id })
        assertEquals(true, viewModel.responderDesafio("EST-001", desafios.first(), 0))
        assertEquals(false, viewModel.responderDesafio("EST-001", desafios.first(), 1))
        assertNull(viewModel.responderDesafio("EST-001", desafios.first(), 99))

        val seguimiento = viewModel.seguimientos.value.single { it.estudianteId == "EST-001" }
        assertEquals(listOf(1, 2), seguimiento.respuestas.map { it.numeroIntento })
        assertEquals(listOf(true, false), seguimiento.respuestas.map { it.esCorrecta })
        assertEquals(EstadoAvance.EN_PROGRESO, seguimiento.estado)
    }

    @Test
    fun evaluacionRespetaMaximoYNoGuardaTrasAgotarlo() {
        val viewModel = MpazProViewModel()
        val preguntas = viewModel.evaluacionFinalDe("C2")
        val respuestas = mapOf("E1" to 0, "E2" to 0)

        assertEquals(listOf("E1", "E2"), preguntas.map { it.id })
        assertEquals(2, viewModel.intentosRestantes("EST-001", "C2"))
        assertTrue(viewModel.enviarEvaluacion("EST-001", "C2", respuestas))
        assertEquals(1, viewModel.intentosRestantes("EST-001", "C2"))
        assertTrue(viewModel.enviarEvaluacion("EST-001", "C2", respuestas))
        assertEquals(0, viewModel.intentosRestantes("EST-001", "C2"))

        val respuestasGuardadas = viewModel.respuestasDe("EST-001", "C2")
        assertFalse(viewModel.enviarEvaluacion("EST-001", "C2", respuestas))
        assertEquals(respuestasGuardadas, viewModel.respuestasDe("EST-001", "C2"))
    }
}
