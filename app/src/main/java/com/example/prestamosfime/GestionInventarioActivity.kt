package com.example.prestamosfime

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GestionInventarioActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val listaHerramientas = mutableListOf<Herramienta>()
    private lateinit var adapter: InventarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_inventario)

        val etNuevoArticulo = findViewById<EditText>(R.id.etNombreHerramienta)
        val btnAgregar = findViewById<Button>(R.id.btnAgregarCatalogo)
        val rvInventario = findViewById<RecyclerView>(R.id.rvInventarioGestion)

        // Configurar la lista visual
        adapter = InventarioAdapter(listaHerramientas) { idDocumento ->
            borrarDelInventario(idDocumento)
        }
        rvInventario.layoutManager = LinearLayoutManager(this)
        rvInventario.adapter = adapter

        // Escuchar cambios en el inventario (Lectura en tiempo real)
        cargarInventario()

        btnAgregar.setOnClickListener {
            val nombre = etNuevoArticulo.text.toString().trim()
            if (nombre.isNotEmpty()) {
                agregarAlInventario(nombre)
                etNuevoArticulo.text.clear()
            }
        }
    }

    private fun cargarInventario() {
        db.collection("inventario").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                listaHerramientas.clear()
                for (doc in snapshots) {
                    val nombre = doc.getString("nombre") ?: ""
                    listaHerramientas.add(Herramienta(doc.id, nombre))
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun agregarAlInventario(nombre: String) {
        val item = hashMapOf("nombre" to nombre, "disponible" to true)
        db.collection("inventario").add(item)
            .addOnSuccessListener { Toast.makeText(this, "Agregado", Toast.LENGTH_SHORT).show() }
    }

    private fun borrarDelInventario(id: String) {
        db.collection("inventario").document(id).delete()
            .addOnSuccessListener { Toast.makeText(this, "Eliminado del sistema", Toast.LENGTH_SHORT).show() }
    }
}