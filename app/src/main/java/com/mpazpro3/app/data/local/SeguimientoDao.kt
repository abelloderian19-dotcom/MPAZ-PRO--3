package com.mpazpro3.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class SeguimientoDao {
    @Query("SELECT * FROM seguimientos")
    abstract suspend fun listarSeguimientos(): List<SeguimientoEntity>

    @Query("SELECT * FROM respuestas")
    abstract suspend fun listarRespuestas(): List<RespuestaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun guardarCabecera(seguimiento: SeguimientoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun guardarRespuestas(respuestas: List<RespuestaEntity>)

    @Query("DELETE FROM respuestas WHERE estudianteId = :estudianteId AND contenidoId = :contenidoId")
    protected abstract suspend fun borrarRespuestas(estudianteId: String, contenidoId: String)

    @Transaction
    open suspend fun guardar(seguimiento: SeguimientoEntity, respuestas: List<RespuestaEntity>) {
        guardarCabecera(seguimiento)
        borrarRespuestas(seguimiento.estudianteId, seguimiento.contenidoId)
        if (respuestas.isNotEmpty()) guardarRespuestas(respuestas)
    }
}
