package com.example.prestamosfime

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Referencias a los componentes de la interfaz
        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val spArticulo = findViewById<Spinner>(R.id.spArticulo)
        val etDias = findViewById<TextInputEditText>(R.id.etDias)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnReportes = findViewById<Button>(R.id.btnVerReportes)
        val btnGestionInventario = findViewById<Button>(R.id.btnGestionInventario)

        // 2. Cargar el catálogo desde Firebase al iniciar
        cargarInventarioDinamico(spArticulo)

        // 3. Botón: Registrar Préstamo
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val articulo = spArticulo.selectedItem?.toString() ?: ""
            val diasString = etDias.text.toString()

            if (nombre.isNotEmpty() && articulo.isNotEmpty() && diasString.isNotEmpty()) {
                procesarPrestamo(nombre, articulo, diasString.toInt())
                etNombre.text?.clear()
                etDias.setText("3") // Valor por defecto
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Botón: Ver Reportes (Tabla de préstamos)
        btnReportes.setOnClickListener {
            val intent = Intent(this, ReportesActivity::class.java)
            startActivity(intent)
        }

        // 5. Botón: Gestionar Catálogo (Agregar o quitar herramientas)
        btnGestionInventario.setOnClickListener {
            val intent = Intent(this, GestionInventarioActivity::class.java)
            startActivity(intent)
        }
    }

    // --- LÓGICA DE FIREBASE ---

    private fun cargarInventarioDinamico(spArticulo: Spinner) {
        // Escucha cambios en tiempo real: si agregas algo en la otra pantalla, aparece aquí solo
        db.collection("inventario")
            .whereEqualTo("disponible", true)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener

                val listaHerramientas = mutableListOf<String>()
                for (doc in snapshots) {
                    val nombre = doc.getString("nombre") ?: ""
                    listaHerramientas.add(nombre)
                }

                val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaHerramientas)
                spArticulo.adapter = adaptador
            }
    }

    private fun procesarPrestamo(nombre: String, articulo: String, dias: Int) {
        // Buscamos el artículo para "apartarlo"
        db.collection("inventario")
            .whereEqualTo("nombre", articulo)
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "Artículo ya no está disponible", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val idDoc = documentos.documents[0].id

                // Marcamos como no disponible
                db.collection("inventario").document(idDoc)
                    .update("disponible", false)
                    .addOnSuccessListener {
                        // Si se apartó con éxito, creamos el ticket de préstamo
                        guardarRegistroFinal(nombre, articulo, dias)
                    }
            }
    }

    private fun guardarRegistroFinal(nombre: String, articulo: String, dias: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dias)

        val prestamo = hashMapOf(
            "alumno" to nombre,
            "articulo" to articulo,
            "fechaPrestamo" to Date(),
            "fechaLimite" to cal.time,
            "estatus" to "PENDIENTE"
        )

        db.collection("prestamos")
            .add(prestamo)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Préstamo registrado!", Toast.LENGTH_SHORT).show()
            }
    }
}