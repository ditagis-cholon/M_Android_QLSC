package hcm.ditagis.com.cholon.qlsc

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.fragment.task.HandlingSearchHasDone
import hcm.ditagis.com.cholon.qlsc.fragment.task.ListTaskFragment
import hcm.ditagis.com.cholon.qlsc.fragment.task.SearchFragment
import java.util.*

class ListTaskActivity : AppCompatActivity() {

    private lateinit var mListTaskFragment: ListTaskFragment
    private lateinit var mSearchFragment: SearchFragment

    private var mApplication: DApplication? = null

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_task)
        mApplication = application as DApplication
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        val viewPager = findViewById<ViewPager>(R.id.container_basemap)
        viewPager.adapter = sectionsPagerAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tabs_basemap)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        mListTaskFragment = ListTaskFragment(this@ListTaskActivity, layoutInflater)
        mSearchFragment = SearchFragment(this@ListTaskActivity, layoutInflater)

        viewPager.setCurrentItem(0, true)
    }

    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return mSearchFragment
                1 -> return mListTaskFragment
                else -> return mSearchFragment
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    fun itemClick(adapter: AdapterView<*>, position: Int) {
        val item = adapter.getItemAtPosition(position) as HandlingSearchHasDone.Item
        val layout = layoutInflater.inflate(R.layout.layout_dialog, null) as LinearLayout
        val txtTitle = layout.findViewById<TextView>(R.id.txt_dialog_title)
        val txtMessage = layout.findViewById<TextView>(R.id.txt_dialog_message)
        txtTitle.text = getString(R.string.message_title_confirm)
        txtMessage.text = getString(R.string.message_click_list_task, item.id)

        val builder = AlertDialog.Builder(this@ListTaskActivity)
        builder.setView(layout)
        builder.setCancelable(false)
                .setPositiveButton(R.string.message_btn_ok) { dialog, i ->
                    mApplication!!.diemSuCo.idSuCo = item.id
                    goHome()
                }.setNegativeButton(R.string.message_btn_cancel) { dialog, i -> }

        val dialog = builder.create()
        dialog.show()
    }

    fun itemClick(id: String) {
        val layout = layoutInflater.inflate(R.layout.layout_dialog, null) as LinearLayout
        val txtTitle = layout.findViewById<TextView>(R.id.txt_dialog_title)
        val txtMessage = layout.findViewById<TextView>(R.id.txt_dialog_message)
        txtTitle.text = getString(R.string.message_title_confirm)
        txtMessage.text = getString(R.string.message_click_list_task, id)

        val builder = AlertDialog.Builder(this@ListTaskActivity)
        builder.setView(layout)
        builder.setCancelable(false)
                .setPositiveButton(R.string.message_btn_ok) { dialog, i ->
                    mApplication!!.diemSuCo.idSuCo = id
                    goHome()
                }.setNegativeButton(R.string.message_btn_cancel) { dialog, i -> }

        val dialog = builder.create()
        dialog.show()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        goHome()
    }


    private fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
