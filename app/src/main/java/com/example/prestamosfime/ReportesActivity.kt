package com.example.prestamosfime

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class ReportesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        val txtMasSolicitado = findViewById<TextView>(R.id.txtMasSolicitado)
        val txtListaMorosos = findViewById<TextView>(R.id.txtListaMorosos)
        val etNombreDevolucion = findViewById<EditText>(R.id.etNombreDevolucion)
        val btnDevolver = findViewById<Button>(R.id.btnDevolver)

        // 1. Cargar los reportes al abrir la pantalla
        cargarMasSolicitado(txtMasSolicitado)
        cargarMorosos(txtListaMorosos)

        // 2. Acción del botón "Recibir"
        btnDevolver.setOnClickListener {
            val nombre = etNombreDevolucion.text.toString()
            if (nombre.isNotEmpty()) {
                procesarDevolucion(nombre, txtListaMorosos)
                etNombreDevolucion.text.clear() // Limpiar la cajita
            } else {
                Toast.makeText(this, "Escribe un nombre", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LA MAGIA: CAMBIAR ESTATUS A "ENTREGADO" ---
    private fun procesarDevolucion(nombre: String, txtListaMorosos: TextView) {
        // Buscamos al alumno en la base de datos
        db.collection("prestamos")
            .whereEqualTo("alumno", nombre)
            .get()
            .addOnSuccessListener { documentos ->
                var encontrado = false

                for (doc in documentos) {
                    // Si encontramos su préstamo y sigue PENDIENTE...
                    if (doc.getString("estatus") == "PENDIENTE") {
                        // ¡Lo actualizamos a ENTREGADO!
                        db.collection("prestamos").document(doc.id)
                            .update("estatus", "ENTREGADO")
                            .addOnSuccessListener {
                                Toast.makeText(this, "¡Artículo recibido!", Toast.LENGTH_SHORT).show()
                                cargarMorosos(txtListaMorosos) // Actualizar lista
                            }
                        encontrado = true
                        break // Solo devolvemos uno por clic
                    }
                }

                if (!encontrado) {
                    Toast.makeText(this, "No hay préstamos pendientes para $nombre", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- REPORTE 1: ARTÍCULO MÁS PEDIDO ---
    private fun cargarMasSolicitado(textView: TextView) {
        db.collection("prestamos").get().addOnSuccessListener { documentos ->
            val conteo = HashMap<String, Int>()
            for (doc in documentos) {
                val articulo = doc.getString("articulo") ?: continue
                conteo[articulo] = conteo.getOrDefault(articulo, 0) + 1
            }
            var ganador = "Ninguno"
            var maxVotos = 0
            for ((articulo, votos) in conteo) {
                if (votos > maxVotos) {
                    maxVotos = votos
                    ganador = articulo
                }
            }
            textView.text = "$ganador ($maxVotos préstamos)"
        }
    }

    // --- REPORTE 2: MOROSOS ---
    private fun cargarMorosos(textView: TextView) {
        db.collection("prestamos")
            .whereEqualTo("estatus", "PENDIENTE")
            .get()
            .addOnSuccessListener { documentos ->
                val lista = StringBuilder()
                val hoy = Date()

                for (doc in documentos) {
                    val fechaLimite = doc.getDate("fechaLimite")
                    // Si la fecha límite es menor a hoy (ya se venció)
                    if (fechaLimite != null && fechaLimite.before(hoy)) {
                        val alumno = doc.getString("alumno")
                        val articulo = doc.getString("articulo")
                        lista.append("• $alumno debe: $articulo\n")
                    }
                }

                if (lista.isEmpty()) {
                    textView.text = "¡Nadie debe nada! 😇"
                } else {
                    textView.text = lista.toString()
                }
            }
            .addOnFailureListener {
                textView.text = "Error cargando lista: ${it.message}"
            }
    }
}