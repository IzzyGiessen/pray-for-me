package com.onlysaints.prayforme

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.onlysaints.prayforme.classes.Prayer
import com.onlysaints.prayforme.database.Database
import com.onlysaints.prayforme.listeners.ButtonTouchListener
import java.io.Serializable

class PrayerAdapter(private val context: Context, val prayers: ArrayList<Prayer>,
                    private val onClickListener: OnClickListener, private val profileOnFinish: ProfileOnFinish)
        : RecyclerView.Adapter<PrayerAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val db: Database = Database()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.prayer_adapter, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return prayers.size
    }

    fun addPrayer(prayer: Prayer) {
        prayers.add(prayer)
        prayers.sort()
        val idx = prayers.indexOf(prayer)
        notifyItemInserted(idx)
        notifyItemRangeChanged(idx, itemCount-idx-1)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prayer = prayers[position]
        holder.prayerId = prayer.id()
        holder.prayerText.text = prayer.text
        holder.prayCount.text = context.resources.getString(R.string.prayers_received, prayer.count().toString())
    }

    fun createRemoveOnClick(viewHolder: ViewHolder): (v: View) -> Unit = {
        profileOnFinish.removePrayer(viewHolder)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), OnLongClickListener {
        val prayerText: TextView
        val prayCount: TextView
        val prayerCard: ConstraintLayout
        val removeButton: ImageButton
        val switcher: ViewSwitcher
        var prayerId: String? = null

        init {
            prayerText = view.findViewById(R.id.prayer_text)
            prayCount = view.findViewById(R.id.pray_count)
            prayerCard = view.findViewById(R.id.prayer_card)
            removeButton = view.findViewById(R.id.remove_button)
            switcher = view.findViewById(R.id.prayer_card_switcher)

            view.setOnTouchListener(ButtonTouchListener())
            view.setOnLongClickListener(::onLongClick)
            view.setOnClickListener(onClickListener)
            // warning is for the blind
            removeButton.setOnTouchListener(ButtonTouchListener())
            removeButton.setOnLongClickListener(::onLongClick)
            removeButton.setOnClickListener(createRemoveOnClick(this))
        }

        override fun onLongClick(v: View): Boolean {
            switcher.showNext()
            return true
        }

        fun remove() {
            // TODO: handle exceptions
            prayers.removeAt(adapterPosition)
            switcher.showNext()
            notifyItemRemoved(adapterPosition)
            db.removePrayer(prayerId!!, {})
        }
    }
}