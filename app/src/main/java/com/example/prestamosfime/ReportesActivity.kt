package com.example.prestamosfime

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ReportesActivity : AppCompatActivity() {

    // Bloque de declaraciones globales
    private lateinit var rvPrestamos: RecyclerView
    private lateinit var prestamoAdapter: PrestamoAdapter
    private val prestamosList = mutableListOf<Prestamo>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var txtMasSolicitado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        // Vinculación de vistas
        txtMasSolicitado = findViewById(R.id.txtMasSolicitado)
        val etNombreDevolucion = findViewById<EditText>(R.id.etNombreDevolucion)
        val btnDevolver = findViewById<Button>(R.id.btnDevolver)

        // Configuración de la Tabla (RecyclerView)
        rvPrestamos = findViewById(R.id.rvPrestamos)
        rvPrestamos.layoutManager = LinearLayoutManager(this)
        prestamoAdapter = PrestamoAdapter(prestamosList)
        rvPrestamos.adapter = prestamoAdapter

        cargarDatos()

        // Acción: Devolver artículo
        btnDevolver.setOnClickListener {
            val nombreInput = etNombreDevolucion.text.toString().trim()

            if (nombreInput.isNotEmpty()) {
                finalizarPrestamo(nombreInput)
                etNombreDevolucion.text.clear()
            } else {
                Toast.makeText(this, "Escribe el nombre del alumno", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarDatos() {
        db.collection("prestamos").addSnapshotListener { documentos, error ->
            if (error != null) {
                Log.e("FirebaseError", "Error al cargar: ${error.message}")
                return@addSnapshotListener
            }

            if (documentos != null) {
                prestamosList.clear()
                val conteoArticulos = HashMap<String, Int>()

                for (doc in documentos) {
                    val p = doc.toObject(Prestamo::class.java)
                    prestamosList.add(p)

                    if (p.articulo.isNotEmpty()) {
                        conteoArticulos[p.articulo] = conteoArticulos.getOrDefault(p.articulo, 0) + 1
                    }
                }
                prestamoAdapter.notifyDataSetChanged()
                actualizarMasPedido(conteoArticulos)
            }
        }
    }

    private fun finalizarPrestamo(nombreAlumno: String) {
        // Buscamos el préstamo PENDIENTE.
        // NOTA: Si en tu Firebase guardas nombres con mayúsculas, escríbelos igual al probar.
        db.collection("prestamos")
            .whereEqualTo("alumno", nombreAlumno)
            .whereEqualTo("estatus", "PENDIENTE")
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "No se encontró préstamo pendiente para: $nombreAlumno", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // Si hay varios, tomamos el primero
                val docPrestamo = documentos.documents[0]
                val idPrestamo = docPrestamo.id
                val nombreArticulo = docPrestamo.getString("articulo") ?: ""

                // PASO 1: Actualizar estatus del préstamo
                db.collection("prestamos").document(idPrestamo)
                    .update("estatus", "ENTREGADO")
                    .addOnSuccessListener {
                        // PASO 2: Liberar la herramienta en el inventario
                        if (nombreArticulo.isNotEmpty()) {
                            liberarArticulo(nombreArticulo)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Error en consulta: ${e.message}")
            }
    }

    private fun liberarArticulo(nombreArticulo: String) {
        db.collection("inventario")
            .whereEqualTo("nombre", nombreArticulo)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val idItem = docs.documents[0].id
                    db.collection("inventario").document(idItem)
                        .update("disponible", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "✅ $nombreArticulo devuelto y disponible", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.d("Inventario", "No se encontró el artículo $nombreArticulo para liberar")
                }
            }
    }

    private fun actualizarMasPedido(conteo: HashMap<String, Int>) {
        var ganador = "Ninguno"
        var max = 0
        for ((art, total) in conteo) {
            if (total > max) {
                max = total
                ganador = art
            }
        }
        txtMasSolicitado.text = "🔥 Lo más pedido: $ganador ($max)"
    }
}