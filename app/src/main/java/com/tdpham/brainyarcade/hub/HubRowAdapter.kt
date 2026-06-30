package com.tdpham.brainyarcade.hub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.HorizontalGridView
import androidx.recyclerview.widget.RecyclerView
import com.tdpham.brainyarcade.R

class HubRowAdapter(
    val rows: List<HubRow>,
    private val dailyGameId: String? = null,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<HubRowAdapter.RowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hub_row, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount(): Int = rows.size

    inner class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val header: TextView = view.findViewById(R.id.row_header)
        private val grid: HorizontalGridView = view.findViewById(R.id.row_grid)

        init {
            grid.setOnChildSelectedListener { _, _, _, _ ->
                // Row zoom effect
                view.animate().scaleX(1.02f).scaleY(1.02f).translationZ(5f).setDuration(400).start()
                header.animate().alpha(1.0f).translationX(20f).setDuration(400).start()
            }
            grid.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(300).start()
                    header.animate().alpha(0.7f).translationX(0f).setDuration(300).start()
                }
            }
            header.alpha = 0.7f
        }

        fun bind(row: HubRow) {
            header.text = row.title
            grid.adapter = GameAdapter(row.games, dailyGameId, onGameClick)
            grid.setNumRows(1)
        }
    }
}
