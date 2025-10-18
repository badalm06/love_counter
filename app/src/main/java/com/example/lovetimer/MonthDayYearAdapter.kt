import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lovetimer.R

class WheelAdapter(
    private var items: List<String>,
    private val onClick: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<WheelAdapter.VH>() {

    private var selectedIndex = -1

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.tvItem)
        init {
            view.setOnClickListener {
                onClick?.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.wheel_item, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = items[position]

        // Increase text size for selected item
        if (position == selectedIndex) {
            holder.tv.textSize = 22f   // highlight size
        } else {
            holder.tv.textSize = 16f   // normal size
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setSelected(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }
}
