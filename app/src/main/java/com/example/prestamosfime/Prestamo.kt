package com.example.prestamosfime

import com.google.firebase.Timestamp

data class Prestamo(
    val alumno: String = "",
    val articulo: String = "",
    val dias: String = "",
    val estatus: String = "PENDIENTE",
    val fechaLimite: Timestamp? = null //
)