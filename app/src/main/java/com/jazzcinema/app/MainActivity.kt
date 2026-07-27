package com.jazzcinema.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jazzcinema.app.databinding.ActivityMainBinding
import com.jazzcinema.app.ui.home.HomeFragment
import com.jazzcinema.app.ui.library.LibraryFragment
import com.jazzcinema.app.ui.profile.ProfileFragment
import com.jazzcinema.app.ui.search.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment     by lazy { HomeFragment() }
    private val searchFragment   by lazy { SearchFragment() }
    private val libraryFragment  by lazy { LibraryFragment() }
    private val profileFragment  by lazy { ProfileFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showFragment(homeFragment, "home")
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { showFragment(homeFragment,    "home");    true }
                R.id.nav_search  -> { showFragment(searchFragment,  "search");  true }
                R.id.nav_library -> { showFragment(libraryFragment, "library"); true }
                R.id.nav_profile -> { showFragment(profileFragment, "profile"); true }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment, tag: String) {
        val fm = supportFragmentManager
        val tx = fm.beginTransaction()

        // Add if not added yet
        if (!fragment.isAdded) {
            tx.add(R.id.fragment_container, fragment, tag)
        }

        // Hide all others
        fm.fragments.forEach { f -> if (f != fragment && f.isAdded) tx.hide(f) }

        tx.show(fragment).commit()
    }
}
