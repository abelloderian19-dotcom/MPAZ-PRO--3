package com.mpazpro3.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.data.local.AppDatabase
import com.mpazpro3.app.ui.screen.LoginScreen
import com.mpazpro3.app.ui.screen.docente.InformeDocenteScreen
import com.mpazpro3.app.ui.screen.docente.PanelDocenteScreen
import com.mpazpro3.app.ui.screen.docente.PlanificacionDocenteScreen
import com.mpazpro3.app.ui.screen.docente.ResultadosCursoScreen
import com.mpazpro3.app.ui.screen.estudiante.DesafiosScreen
import com.mpazpro3.app.ui.screen.estudiante.EvaluacionFinalScreen
import com.mpazpro3.app.ui.screen.estudiante.InicioEstudianteScreen
import com.mpazpro3.app.ui.screen.estudiante.MaterialScreen
import com.mpazpro3.app.ui.screen.estudiante.RevisionRespuestasScreen
import com.mpazpro3.app.ui.screen.utp.ContenidosUtpScreen
import com.mpazpro3.app.ui.screen.utp.DetalleContenidoUtpScreen
import com.mpazpro3.app.viewmodel.MpazProViewModel

object Rutas {
    const val LOGIN = "login"

    const val UTP_CONTENIDOS = "utp_contenidos"
    const val UTP_DETALLE = "utp_detalle"

    const val DOCENTE_PANEL = "docente_panel"
    const val DOCENTE_PLANIFICACION = "docente_planificacion"
    const val DOCENTE_RESULTADOS = "docente_resultados"
    const val DOCENTE_INFORME = "docente_informe"

    const val ESTUDIANTE_INICIO = "estudiante_inicio"
    const val ESTUDIANTE_MATERIAL = "estudiante_material"
    const val ESTUDIANTE_DESAFIOS = "estudiante_desafios"
    const val ESTUDIANTE_EVALUACION = "estudiante_evaluacion"
    const val ESTUDIANTE_REVISION = "estudiante_revision"
}

/**
 * Flujo de pantallas (un solo NavHost y un único MpazProViewModel compartido):
 *
 *   LOGIN ──(según perfil)──> UTP_CONTENIDOS ──> UTP_DETALLE
 *                        ──> DOCENTE_PANEL ──> DOCENTE_PLANIFICACION
 *                                          ──> DOCENTE_RESULTADOS ──> DOCENTE_INFORME
 *                        ──> ESTUDIANTE_INICIO ──> MATERIAL ──> DESAFIOS ──> EVALUACION ──> REVISION
 *
 * El contenido y el estudiante elegidos se comparten a través del ViewModel (como en la
 * Guía 11): no se pasan argumentos por la ruta.
 *
 * Regla del caso: el estudiante nunca puede llegar a DOCENTE_INFORME (ninguna pantalla de su
 * flujo navega hacia allá).
 *
 * Botón atrás: el atrás nativo de Android funciona solo (lo maneja el NavHost). Al ingresar,
 * LOGIN sale de la pila; "Cerrar sesión" limpia la pila completa.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    injectedViewModel: MpazProViewModel? = null
) {
    val appContext = LocalContext.current.applicationContext
    val sharedViewModel = injectedViewModel ?: viewModel(factory = viewModelFactory {
        initializer {
            MpazProViewModel(AppDatabase.getInstance(appContext).seguimientoDao())
        }
    })
    val cerrarSesion: () -> Unit = {
        sharedViewModel.logout()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = sharedViewModel,
                onIngresar = { perfil ->
                    val destino = when (perfil) {
                        Perfil.UTP -> Rutas.UTP_CONTENIDOS
                        Perfil.DOCENTE -> Rutas.DOCENTE_PANEL
                        Perfil.ESTUDIANTE -> Rutas.ESTUDIANTE_INICIO
                    }
                    navController.navigate(destino) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ---- UTP ----
        composable(Rutas.UTP_CONTENIDOS) {
            ContenidosUtpScreen(navController, sharedViewModel, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.UTP_DETALLE) {
            DetalleContenidoUtpScreen(navController, sharedViewModel)
        }

        // ---- Docente ----
        composable(Rutas.DOCENTE_PANEL) {
            PanelDocenteScreen(navController, sharedViewModel, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.DOCENTE_PLANIFICACION) {
            PlanificacionDocenteScreen(navController, sharedViewModel)
        }
        composable(Rutas.DOCENTE_RESULTADOS) {
            ResultadosCursoScreen(navController, sharedViewModel)
        }
        composable(Rutas.DOCENTE_INFORME) {
            InformeDocenteScreen(navController, sharedViewModel)
        }

        // ---- Estudiante ----
        composable(Rutas.ESTUDIANTE_INICIO) {
            InicioEstudianteScreen(navController, sharedViewModel, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.ESTUDIANTE_MATERIAL) {
            MaterialScreen(navController, sharedViewModel)
        }
        composable(Rutas.ESTUDIANTE_DESAFIOS) {
            DesafiosScreen(navController, sharedViewModel)
        }
        composable(Rutas.ESTUDIANTE_EVALUACION) {
            EvaluacionFinalScreen(navController, sharedViewModel)
        }
        composable(Rutas.ESTUDIANTE_REVISION) {
            RevisionRespuestasScreen(navController, sharedViewModel)
        }
    }
}
