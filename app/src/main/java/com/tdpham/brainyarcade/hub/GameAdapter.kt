package com.tdpham.brainyarcade.hub

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tdpham.brainyarcade.R

class GameAdapter(
    private val games: List<GameInfo>,
    private val dailyGameId: String? = null,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size

    inner class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.game_title)
        private val benefit: TextView = view.findViewById(R.id.game_benefit)
        private val banner: ImageView = view.findViewById(R.id.game_banner)
        private val shortDesc: TextView = view.findViewById(R.id.game_short_desc)
        private val bgGradient: View = view.findViewById(R.id.game_bg_gradient)
        private val decoration: ImageView = view.findViewById(R.id.game_decoration)
        private val focusBorder: View = view.findViewById(R.id.focus_border)
        private val dailyBadge: View = view.findViewById(R.id.daily_badge)

        init {
            view.setOnClickListener { onGameClick(games[adapterPosition]) }

            view.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    // Dramatic Zoom & Elevation
                    v.animate().scaleX(1.15f).scaleY(1.15f).translationZ(20f).setDuration(400)
                        .setInterpolator(OvershootInterpolator(0.8f)).start()
                    
                    // Reveal Description with Fade
                    shortDesc.animate().alpha(1.0f).translationY(0f).setDuration(400).start()
                    
                    // Subtle Background Parallax
                    decoration.animate().scaleX(1.3f).scaleY(1.3f).alpha(0.25f).setDuration(3000)
                        .setInterpolator(AccelerateDecelerateInterpolator()).start()
                    
                    banner.animate().scaleX(1.1f).scaleY(1.1f).translationY(-15f).setDuration(400).start()
                    
                    // Cancel any previous pulse animation
                    (focusBorder.tag as? android.animation.ValueAnimator)?.cancel()
                    
                    focusBorder.animate().alpha(1.0f).setDuration(400).withEndAction {
                        if (v.isFocused) {
                            val pulse = android.animation.ValueAnimator.ofFloat(0.5f, 1.0f).apply {
                                duration = 1200
                                repeatCount = android.animation.ValueAnimator.INFINITE
                                repeatMode = android.animation.ValueAnimator.REVERSE
                                addUpdateListener { animator ->
                                    if (v.isFocused) {
                                        focusBorder.alpha = animator.animatedValue as Float
                                    } else {
                                        cancel()
                                    }
                                }
                            }
                            focusBorder.tag = pulse
                            pulse.start()
                        }
                    }.start()
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(300).start()
                    shortDesc.animate().alpha(0.0f).translationY(10f).setDuration(200).start()
                    decoration.animate().scaleX(1.0f).scaleY(1.0f).alpha(0.1f).setDuration(300).start()
                    banner.animate().scaleX(1.0f).scaleY(1.0f).translationY(0f).setDuration(300).start()
                    
                    // Cancel breathing pulse and fade out
                    (focusBorder.tag as? android.animation.ValueAnimator)?.cancel()
                    focusBorder.animate().alpha(0.0f).setDuration(300).start()
                }
            }
        }

        fun bind(game: GameInfo) {
            val context = itemView.context
            title.text = context.getString(game.titleResId).uppercase()
            benefit.text = game.benefits.firstOrNull()?.name?.replace("_", " ") ?: ""
            shortDesc.text = context.getString(game.descriptionResId)
            shortDesc.translationY = 15f 
            
            if (game.id == dailyGameId) {
                dailyBadge.visibility = View.VISIBLE
                dailyBadge.animate().scaleX(1.1f).scaleY(1.1f).setDuration(800)
                    .setInterpolator(android.view.animation.CycleInterpolator(1000f)).start()
            } else {
                dailyBadge.visibility = View.GONE
                dailyBadge.animate().cancel()
            }
            
            val startColor = when(game.category) {
                GameCategory.LOGIC -> ContextCompat.getColor(context, R.color.grad_logic_start)
                GameCategory.MATH -> ContextCompat.getColor(context, R.color.grad_math_start)
                GameCategory.SPATIAL -> ContextCompat.getColor(context, R.color.grad_spatial_start)
                GameCategory.MEMORY -> ContextCompat.getColor(context, R.color.grad_memory_start)
                GameCategory.WORDS -> ContextCompat.getColor(context, R.color.grad_words_start)
                GameCategory.STRATEGY -> ContextCompat.getColor(context, R.color.grad_strategy_start)
            }
            val midColor = when(game.category) {
                GameCategory.LOGIC -> ContextCompat.getColor(context, R.color.grad_logic_mid)
                GameCategory.MATH -> ContextCompat.getColor(context, R.color.grad_math_mid)
                GameCategory.SPATIAL -> ContextCompat.getColor(context, R.color.grad_spatial_mid)
                GameCategory.MEMORY -> ContextCompat.getColor(context, R.color.grad_memory_mid)
                GameCategory.WORDS -> ContextCompat.getColor(context, R.color.grad_words_mid)
                GameCategory.STRATEGY -> ContextCompat.getColor(context, R.color.grad_strategy_mid)
            }
            val endColor = when(game.category) {
                GameCategory.LOGIC -> ContextCompat.getColor(context, R.color.grad_logic_end)
                GameCategory.MATH -> ContextCompat.getColor(context, R.color.grad_math_end)
                GameCategory.SPATIAL -> ContextCompat.getColor(context, R.color.grad_spatial_end)
                GameCategory.MEMORY -> ContextCompat.getColor(context, R.color.grad_memory_end)
                GameCategory.WORDS -> ContextCompat.getColor(context, R.color.grad_words_end)
                GameCategory.STRATEGY -> ContextCompat.getColor(context, R.color.grad_strategy_end)
            }
            
            val gd = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(startColor, midColor, endColor))
            gd.cornerRadius = context.resources.getDimension(R.dimen.card_corner_radius)
            bgGradient.background = gd
            banner.setImageResource(game.iconResId)
            
            // Texture decoration
            decoration.setImageResource(game.bannerResId)
            decoration.rotation = (game.id.hashCode() % 30).toFloat()
            decoration.alpha = 0.08f
        }
    }
}
