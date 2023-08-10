package ru.afinogeev.lettersreplacement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ru.afinogeev.lettersreplacement.alphabet.Alphabet
import ru.afinogeev.lettersreplacement.utils.Assets
import java.io.*


open class MainActivity : AppCompatActivity() {

    private lateinit var adapter: LrPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val tabNames: Array<String> = arrayOf(
        "Редактор",
        "Алфавит",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = LrPagerAdapter(this)
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = adapter

        tabLayout = findViewById(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()

        checkAlphabet()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_contacts -> {
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
                return true
            }
            android.R.id.home-> {
                finish()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkAlphabet(){
        if(!File(applicationContext.getExternalFilesDir(null), Alphabet.filename).canRead())
            Assets.copyAssets(applicationContext)
        Alphabet.readFile(applicationContext)
    }

}

class LrPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = WorkplaceFragment()
            1 -> fragment = AlphabetFragment()
        }
        return fragment!!
    }
}