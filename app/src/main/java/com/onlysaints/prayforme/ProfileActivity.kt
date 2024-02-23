package com.onlysaints.prayforme

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.onlysaints.prayforme.database.Database
import com.onlysaints.prayforme.database.LocalStorage
import com.onlysaints.prayforme.listeners.ButtonTouchListener
import java.util.logging.Logger

class ProfileActivity : AppCompatActivity(), OnClickListener {
    val db = Database()
    val ls = LocalStorage(this)
    private lateinit var o: ProfileOnFinish

    private lateinit var writePopupWindow: PopupWindow
    private lateinit var readPopupWindow: PopupWindow

    // popup views
    lateinit var profileLayout: ConstraintLayout
    private lateinit var lineCount: TextView
    private lateinit var prayerEditText: EditText
    private lateinit var prayerText: TextView
    private lateinit var prayCount: TextView
    private lateinit var prayerCard: ConstraintLayout

    // views
    lateinit var prayerAdapter: PrayerAdapter
    lateinit var prayerRecycler: RecyclerView
    lateinit var alternativeText: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        o = ProfileOnFinish(this)

        // initialize views
        val backButton = findViewById<ImageButton>(R.id.back_button)
        alternativeText = findViewById(R.id.alternative_text)
        profileLayout = findViewById(R.id.profile_layout)

        // initialize recyclerview
        prayerAdapter = PrayerAdapter(this, arrayListOf(), o)
        prayerRecycler = findViewById(R.id.prayer_recycler)
        prayerRecycler.layoutManager = GridLayoutManager(this, 2)
        prayerRecycler.adapter = prayerAdapter
        loadPrayers()

        // initialize write popup
        val writePopupView = layoutInflater.inflate(R.layout.write_prayer_popup, null, false)
        val writePopupMargin = writePopupView.findViewById<ConstraintLayout>(R.id.popup_margin)
        lineCount = writePopupView.findViewById(R.id.line_count)
        lineCount.text = resources.getString(R.string.line_count, "0")
        prayerEditText = writePopupView.findViewById(R.id.prayer_text)
        prayerEditText.addTextChangedListener(WritePrayerWatcher())
        writePopupMargin.setOnClickListener{ writePopupWindow.dismiss() }
        writePopupWindow = PopupWindow(writePopupView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true)
        writePopupWindow.animationStyle = R.style.popupAnimation

        // initialize read popup
        val readPopupView = layoutInflater.inflate(R.layout.view_prayer_popup, null)
        val readPopupMargin = readPopupView.findViewById<ConstraintLayout>(R.id.popup_margin)
        prayerText = readPopupView.findViewById(R.id.prayer_text)
        prayCount = readPopupView.findViewById(R.id.pray_count)
        prayerCard = readPopupView.findViewById(R.id.prayer_card)
        readPopupMargin.setOnClickListener{ readPopupWindow.dismiss() }
        readPopupWindow = PopupWindow(readPopupView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true)
        readPopupWindow.animationStyle = R.style.popupAnimation

        // set onTouchListeners
        backButton.setOnTouchListener(ButtonTouchListener())
    }

    private fun loadPrayers() {
        //o.prayers.clear()
        val prayerIdsString = ls.readPrayerIds() ?: return
        val prayerIds = prayerIdsString.split("\n")
        prayerIds.forEach {id ->
            if (ls.has(id)) {
                try {
                    val prayer = ls.readPrayer(id)!!
                    db.getPrayerCount(id, o.addPrayerToAdapter(id, prayer["prayer_text"]!!))
                        {o.addPrayerToAdapter(id, prayer["prayer_text"]!!, prayer["prayed_count"]!!)}
                } catch (e: Exception) {
                    ls.remove(id)
                    e.printStackTrace()
                }
            } else {
                return
            }
        }
    }

    fun requestPrayers(v: View) {
        writePopupWindow.showAtLocation(profileLayout, Gravity.CENTER, 0, 0)
    }

    fun uploadRequest(v: View) {
        val prayer = hashMapOf(
            "name" to "Me",
            "prayer" to prayerEditText.text.toString()
        )
        db.addPrayer(prayer, ::savePrayerLocally)
        writePopupWindow.dismiss()
        prayerEditText.text.clear()
    }

    private fun savePrayerLocally(doc: DocumentReference) {
        db.setPostTime(doc.id)
        db.incPrayerCount(doc.id)
        ls.addPrayerId(doc.id)
        val prayer = HashMap<String, String>()
        prayer["prayer_text"] = prayerEditText.text.toString()
        prayer["prayed_count"] = "0"
        ls.writePrayer(doc.id, prayer)
        prayerAdapter.addPrayer(prayer)
        //loadPrayers()
    }

    fun exit(v: View) {
        finish()
    }

    override fun onClick(v: View) {
        prayerText.text = v.findViewById<TextView>(R.id.prayer_text).text
        prayCount.text = v.findViewById<TextView>(R.id.pray_count).text
        readPopupWindow.showAtLocation(profileLayout, Gravity.CENTER, 0, 0)
    }

    fun savePrayer(v: View) {
        val bm = viewToBitmap(prayerCard)
        ls.downloadImage(bm)
    }

    private fun viewToBitmap(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null)
            bgDrawable.draw(canvas) else
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        return returnedBitmap
    }

    inner class WritePrayerWatcher : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
            lineCount.text = resources.getString(R.string.line_count, text.lines().size.toString())
        }

        override fun afterTextChanged(p0: Editable?) {
        }
    }

    companion object {
        val LOG = Logger.getLogger(ProfileActivity::class.java.name)
    }
}