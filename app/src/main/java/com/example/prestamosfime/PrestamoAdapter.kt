package com.example.prestamosfime

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Date

// El adaptador recibe una lista de préstamos para mostrar
class PrestamoAdapter(private val prestamosList: List<Prestamo>) :
    RecyclerView.Adapter<PrestamoAdapter.PrestamoViewHolder>() {

    // Conecta el codigo con los IDs del XML
    class PrestamoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreAlumno)
        val tvArticulo: TextView = itemView.findViewById(R.id.tvArticulo)
        val tvDias: TextView = itemView.findViewById(R.id.tvDias)
    }

    // Le dice al adaptador que XML usar como molde
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrestamoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prestamo, parent, false)
        return PrestamoViewHolder(view)
    }

    // Pinta los datos en la pantalla fila por fila (Aquí vive la nueva lógica)
    override fun onBindViewHolder(holder: PrestamoViewHolder, position: Int) {
        val prestamo = prestamosList[position]
        holder.tvNombre.text = prestamo.alumno
        holder.tvArticulo.text = prestamo.articulo

        // Logica de Alerta de Fechas
        val hoy = Date()
        val fechaEntrega = prestamo.fechaLimite?.toDate()

        if (prestamo.estatus == "PENDIENTE" && fechaEntrega != null && fechaEntrega.before(hoy)) {
            // Si el estatus es pendiente y la fecha de entrega fue ANTES que hoy
            holder.tvDias.text = "ENTREGA TARDÍA"
            holder.tvDias.setTextColor(Color.RED)
            holder.itemView.setBackgroundColor(Color.parseColor("#FFEBEE")) // Fondo rojizo

        } else if (prestamo.estatus == "ENTREGADO") {
            // Si ya lo entregó, lo marca en verde
            holder.tvDias.text = "DEVUELTO"
            holder.tvDias.setTextColor(Color.parseColor("#388E3C"))
            holder.itemView.setBackgroundColor(Color.WHITE)

        } else {
            // Préstamo normal a tiempo
            holder.tvDias.text = "Días prestado: ${prestamo.dias}"
            holder.tvDias.setTextColor(Color.BLACK)
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
    }

    // Le dice a la lista cuántos elementos hay en total
    override fun getItemCount(): Int {
        return prestamosList.size
    }
}