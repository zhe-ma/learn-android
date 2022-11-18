package com.example.learnandroid.activity

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learnandroid.R
import com.example.learnandroid.ShadowDialog
import com.example.learnandroid.utils.vibrate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.roundToInt


class ShadowFrameLayoutActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ShadowFrameLayoutActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shadowlayout)

        val button = findViewById<Button>(R.id.shadowButton)
        button.setOnClickListener {
            val dialog = ShadowDialog(this)
            dialog.show()
        }

        val seekBarNumber = findViewById<TextView>(R.id.seekBarNumber)
        val seekBar = findViewById<SeekBar>(R.id.shadowSeekbar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var canVibrate = true
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarNumber.text = "$progress"
                if (canVibrate && progress == 100) {
                    vibrate(100)
                    canVibrate = false
                } else if (progress in 95..105) {  // 95-105之间吸附到100
                    seekBar?.progress = 100
                } else {
                    canVibrate = true
                }
                Log.d(TAG, progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })


        val button2 = findViewById<Button>(R.id.BottomSheetButton)
        button2.setOnClickListener {
            val dialog = BlankBottomSheetDialogFragment()
            dialog.show(supportFragmentManager, "BlankBottomSheetDialogFragment")
        }
    }
}


class CategoryAdapter(val categories: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.fruit_category_item, parent, false)
        ) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val text = holder.itemView.findViewById<TextView>(R.id.fruit_categoty)
        text.text = categories[position]
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}

class BlankBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var categoryRv: RecyclerView? = null
    private var categoryLayoutManager: LinearLayoutManager? = null
    private var categoryAdapter: CategoryAdapter? = null
    private var contentRv: RecyclerView? = null
    private var contentLayoutManager: GridLayoutManager? = null

    private var categories = mutableListOf<String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED // 全屏展开
        dialog.behavior.skipCollapsed = true // 设置不折叠
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.activity_bottom_sheet_layout, container, false)
        initView(rootView)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogBg)
    }

    override fun onStart() {
        super.onStart()
        view?.layoutParams?.height = (0.76 * resources.displayMetrics.heightPixels.toFloat()).roundToInt()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    private fun initView(rootView: View) {
        initData()

        categoryRv = rootView.findViewById<RecyclerView?>(R.id.categoryRv).apply {
            categoryLayoutManager = LinearLayoutManager(this@BlankBottomSheetDialogFragment.context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = categoryLayoutManager
            categoryAdapter = CategoryAdapter(categories)
            adapter = categoryAdapter
        }

        contentRv = rootView.findViewById<RecyclerView?>(R.id.contentRv).apply {
            contentLayoutManager = GridLayoutManager(context, 12) // 等分成12份
            layoutManager = contentLayoutManager
        }
    }

    private fun initData() {
        categories = mutableListOf("AAFruit0", "AAFruit2", "AAFruit3", "AAFruit4", "AAFruit5", "AAFruit6")


    }
}


/**
 * 水果数据类
 */
data class Fruit(
    var type: String,
    var imageId: Int,
    var name: String,
    var category: String
)
