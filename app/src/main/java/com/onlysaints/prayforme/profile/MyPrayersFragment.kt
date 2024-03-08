package com.onlysaints.prayforme.profile

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.onlysaints.prayforme.MainActivity
import com.onlysaints.prayforme.R
import com.onlysaints.prayforme.classes.Prayer
import com.onlysaints.prayforme.database.Database
import com.onlysaints.prayforme.database.LocalStorage

class MyPrayersFragment : Fragment(), OnClickListener {
    val o = ProfileOnFinish(this)
    val db = Database()
    lateinit var ls: LocalStorage

    private lateinit var writePopupView: View
    private lateinit var readPopupWindow: PopupWindow

    // popup views
    private lateinit var lineCount: TextView
    private lateinit var prayerEditText: EditText
    private lateinit var prayerText: TextView
    private lateinit var prayCount: TextView
    private lateinit var prayerCard: ConstraintLayout

    // views
    lateinit var prayerAdapter: PrayerAdapter
    lateinit var prayerRecycler: RecyclerView
    lateinit var alternativeText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_my_prayers, container, false)
        alternativeText = view.findViewById(R.id.alternative_text)
        ls = LocalStorage(requireContext())

        prayerAdapter = PrayerAdapter(requireContext(), MainActivity.prayerList, this, o)
        if (prayerAdapter.prayers.isEmpty())
            loadPrayers()
        else
            alternativeText.visibility = View.INVISIBLE

        // initialize views
        val requestPrayersButton = view.findViewById<ImageButton>(R.id.request_prayers_button)

        // initialize recyclerview
        prayerRecycler = view.findViewById(R.id.prayer_recycler)
        prayerRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        prayerRecycler.adapter = prayerAdapter

        // initialize write popup
        writePopupView = layoutInflater.inflate(R.layout.write_prayer_popup, (requireActivity() as ProfileActivity).profileLayout, false)
        val closePopupButton = writePopupView.findViewById<ImageButton>(R.id.exit_button)
        closePopupButton.setOnClickListener(::closeRequest)
        val uploadRequestButton = writePopupView.findViewById<ImageButton>(R.id.request_prayer_button)
        uploadRequestButton.setOnClickListener(::uploadRequest)
        lineCount = writePopupView.findViewById(R.id.line_count)
        lineCount.text = resources.getString(R.string.line_count, "0")
        prayerEditText = writePopupView.findViewById(R.id.prayer_text)
        prayerEditText.addTextChangedListener(WritePrayerWatcher())
        val writePopupMargin = writePopupView.findViewById<ConstraintLayout>(R.id.popup_margin)
        writePopupMargin.setOnClickListener{ removePopupView(writePopupView) }

        // initialize read popup
        val readPopupView = layoutInflater.inflate(R.layout.view_prayer_popup, (requireActivity() as ProfileActivity).profileLayout, false)
        val closePrayerButton = readPopupView.findViewById<ImageButton>(R.id.exit_button)
        closePrayerButton.setOnClickListener(::closePrayer)
        val savePrayerButton = readPopupView.findViewById<ImageButton>(R.id.save_prayer_button)
        savePrayerButton.setOnClickListener(::savePrayer)
        prayerText = readPopupView.findViewById(R.id.prayer_text)
        prayCount = readPopupView.findViewById(R.id.pray_count)
        prayerCard = readPopupView.findViewById(R.id.prayer_card)
        val readPopupMargin = readPopupView.findViewById<ConstraintLayout>(R.id.popup_margin)
        readPopupMargin.setOnClickListener{ readPopupWindow.dismiss() }
        readPopupWindow = PopupWindow(readPopupView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true)
        readPopupWindow.animationStyle = R.style.popupAnimation

        // set onClickListeners
        requestPrayersButton.setOnClickListener(::requestPrayers)
        return view
    }

    private fun loadPrayers() {
        val prayerIdsString = ls.readPrayerIds() ?: return
        val prayerIds = prayerIdsString.split("\n")
        prayerIds.forEach {id ->
            if (ls.has(id)) {
                val prayer = ls.readPrayer(id) ?: return
                db.getPrayerCount(id, o.addCountToPrayer(prayer)) {addPrayerToAdapter(prayer)}
                alternativeText.visibility = View.INVISIBLE
            } else {
                ls.removePrayerId(id)
            }
        }
    }

    fun addPrayerToAdapter(prayer: Prayer) {
        prayerAdapter.addPrayer(prayer)
        prayerRecycler.smoothScrollToPosition(0)
    }

    fun requestPrayers(v: View) {
        (requireActivity() as ProfileActivity).profileLayout.addView(writePopupView)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.popup_show)
        writePopupView.startAnimation(anim)
    }

    private fun removePopupView(v: View) {
        (requireActivity() as ProfileActivity).profileLayout.removeView(v)
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.popup_show)
        v.startAnimation(anim)
        requireActivity().currentFocus?.let { view ->
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun uploadRequest(v: View) {
        val prayerText = prayerEditText.text.toString()
        if (prayerText.trim().isEmpty()) {
            val toast = Toast(requireContext())
            toast.setText("You can not submit empty prayers.")
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
            return
        }
        if (prayerText.count() > 500) {
            val toast = Toast(requireContext())
            toast.setText("Please keep your prayer length below 15 lines.")
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
            return
        }
        val prayer = Prayer(prayerText)
        db.addPrayer(prayer, ::savePrayerLocally)
    }

    fun closeRequest(v: View) {
        removePopupView(writePopupView)
    }

    fun closePrayer(v: View) {
        readPopupWindow.dismiss()
    }

    private fun savePrayerLocally(doc: DocumentReference) {
        db.setPostTime(doc.id)
        db.incPrayerCount(doc.id)
        val prayer = Prayer(prayerEditText.text.toString(), doc.id, 0, System.currentTimeMillis())
        ls.writePrayer(prayer)
        ls.addPrayerId(doc.id)
        prayerEditText.text.clear()
        removePopupView(writePopupView)
        addPrayerToAdapter(prayer)
    }

    // onClick method for prayers in Profile to show full prayer
    override fun onClick(v: View) {
        prayerText.verticalScrollbarPosition = 0
        prayerText.text = v.findViewById<TextView>(R.id.prayer_text).text
        prayerText.movementMethod = ScrollingMovementMethod()
        prayCount.text = v.findViewById<TextView>(R.id.pray_count).text
        readPopupWindow.showAtLocation((requireActivity() as ProfileActivity).profileLayout, Gravity.CENTER, 0, 0)
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
            val chars = text.count()
            lineCount.text = resources.getString(R.string.line_count, chars.toString())
            if (chars > 500) {
                lineCount.setTextColor(resources.getColor(R.color.fairy_tale, null))
            }
        }

        override fun afterTextChanged(p0: Editable?) {
        }
    }

}