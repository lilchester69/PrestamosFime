package com.example.prestamosfime

import android.os.Bundle
import android.widget.Button
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

        // Referencias a los controles
        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etArticulo = findViewById<TextInputEditText>(R.id.etArticulo)
        val etDias = findViewById<TextInputEditText>(R.id.etDias)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnReportes = findViewById<Button>(R.id.btnVerReportes)

        // Acción del botón Guardar
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val articulo = etArticulo.text.toString()
            val diasString = etDias.text.toString()

            if (nombre.isNotEmpty() && articulo.isNotEmpty() && diasString.isNotEmpty()) {
                guardarEnFirebase(nombre, articulo, diasString.toInt())

                // Limpiar campos
                etNombre.text?.clear()
                etArticulo.text?.clear()
                etDias.setText("3")
            } else {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción del botón Reportes (¡AQUÍ ESTÁ EL CAMBIO!)
        btnReportes.setOnClickListener {
            val intent = android.content.Intent(this, ReportesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun guardarEnFirebase(nombre: String, articulo: String, dias: Int) {
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
                Toast.makeText(this, "¡Guardado con éxito!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}