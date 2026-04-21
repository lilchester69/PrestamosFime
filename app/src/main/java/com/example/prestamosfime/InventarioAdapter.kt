package com.example.prestamosfime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Clase modelo para los datos
data class Herramienta(val id: String = "", val nombre: String = "")

class InventarioAdapter(
    private val lista: MutableList<Herramienta>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    class InventarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreItem)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnBorrarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario, parent, false)
        return InventarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre

        // Acción de borrar cuando le picas al botoncito de la basura
        holder.btnBorrar.setOnClickListener {
            onDeleteClick(item.id)
        }
    }

    override fun getItemCount() = lista.size
}