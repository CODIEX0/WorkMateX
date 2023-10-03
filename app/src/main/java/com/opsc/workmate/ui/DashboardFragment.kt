package com.opsc.workmate.ui

import android.content.Intent
import com.opsc.workmate.data.EntryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.opsc.workmate.MainActivity
import com.opsc.workmate.R
import com.opsc.workmate.data.Category
import com.opsc.workmate.data.CategoryAdapter
import com.opsc.workmate.data.DataManager
import com.opsc.workmate.data.Entry
import com.opsc.workmate.data.Global
import com.opsc.workmate.data.Global.categories
import com.opsc.workmate.data.Global.currentUser
import com.opsc.workmate.data.Global.entries
import com.opsc.workmate.data.Global.users


class DashboardFragment :   Fragment(), EntryAdapter.OnItemClickListener, CategoryAdapter.OnItemClickListener {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var work_coins: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        //Set current user Work Coins
        work_coins = view.findViewById(R.id.txtWorkCoins)

        //implement RecyclerView for Categories -------------
        // Find the RecyclerView by ID
        val lstCategories: RecyclerView = view.findViewById(R.id.lstCategories)

        // Create and set the CategoryAdapter with the Global.categories list
        //Moved to onViewCreated to add onclicklistener

        // Create and set the EntryAdapter with the Global.entries list
        //Moved to onViewCreated to add onclicklistener

        //---- Navigate to Create Category screen ----
        // Find the btn by ID
        val btnCreate: Button = view.findViewById(R.id.btnCreate)

        // Set onClickListener for the TextView
        btnCreate.setOnClickListener {
            // Get the NavController
            val navController = Navigation.findNavController(view)

            // Navigate to the CreateCategoryFragment
            navController.navigate(R.id.action_dashboardFragment_to_createCategoryFragment)
        }

        //---- Navigate to Add Entry screen ----
        // Find the btn by ID
        val btnAddEntry: Button = view.findViewById(R.id.btnEntry)

        // Set onClickListener for the TextView
        btnAddEntry.setOnClickListener {
            // Get the NavController
            val navController = Navigation.findNavController(view)

            // Navigate to the CreateCategoryFragment
            navController.navigate(R.id.action_dashboardFragment_to_newEntryFragment)
        }

        //Sing Out button Logic
        auth = FirebaseAuth.getInstance()
        val btnSignout: Button = view.findViewById(R.id.btnSignout) // Find the btn by ID
        btnSignout.setOnClickListener {

            //signing the current user out of firebase
            auth.signOut()

            Toast.makeText(context,"Signing out...", Toast.LENGTH_SHORT).show()
            //navigate to login ACTIVITY
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lstEntries: RecyclerView = view.findViewById(R.id.lstEntries)

        // Set up the LinearLayoutManager for the RecyclerView
        val entryLayoutManager = LinearLayoutManager(requireContext())
        lstEntries.layoutManager = entryLayoutManager

        try{
            DataManager.getWorkcoins(currentUser!!.uid.toString()){ coins ->
                work_coins.text = StringBuilder().append(coins).toString()
            }
        }catch (e: Exception){
            Log.d("coins exception",e.message.toString())
        }


        // Create an instance of EntryAdapter
        DataManager.getEntries(currentUser!!.uid.toString()) { entries ->
            // Update the global categories list
            Global.entries = entries

            // Create an instance of CategoryAdapter and pass the OnItemClickListener
            val entryAdapter = EntryAdapter(Global.entries)
            entryAdapter.setOnItemClickListener(this)
            // Set the adapter to the RecyclerView
            lstEntries.adapter = entryAdapter
        }

        val lstCategories: RecyclerView = view.findViewById(R.id.lstCategories)

        // Set up the LinearLayoutManager for the RecyclerView
        val catLayoutManager = LinearLayoutManager(requireContext())
        lstCategories.layoutManager = catLayoutManager

        // Retrieve updated Categories
        DataManager.getCategories(Global.currentUser!!.uid.toString()) { categories ->
            // Update the global categories list
            Global.categories = categories

            // Create an instance of CategoryAdapter and pass the OnItemClickListener
            val catAdapter = CategoryAdapter(Global.categories, this)

            // Set the adapter to the RecyclerView
            lstCategories.adapter = catAdapter
        }

    }

    // Implementation of the onItemClick method from the OnItemClickListener interface
    override fun onItemClick(entry: Entry) {
        // Handle the click event and navigate to a different fragment
        //Add data to bundle
        val bundle = Bundle()
        bundle.putString("UID", entry.uid)
        bundle.putString("category", entry.categoryName)
        bundle.putString("date", entry.date)
        bundle.putString("startTime", entry.startTime)
        bundle.putString("endTime", entry.endTime)
        bundle.putString("imageData", entry.imageData)
        bundle.putString("description", entry.description)

        val fragment = EntryFragment()
        fragment.arguments = bundle

        //Navigate to fragment, passing bundle
        findNavController().navigate(R.id.action_dashboardFragment_to_entryFragment, bundle)
    }

    // Implementation of the onItemClick method from the CategoryAdapter.OnItemClickListener interface
    override fun onItemClick(category: Category) {
        // Handle the click event and navigate to a different fragment
        //Add data to bundle
        val bundle = Bundle()
        bundle.putString("UID", category.UID)
        bundle.putString("name", category.name)
        category.colour?.let { bundle.putInt("colour", it) }
        bundle.putString("imageData", category.imageData)

        val fragment = CategoryDetailsFragment()
        fragment.arguments = bundle

        //Navigate to fragment, passing bundle
        findNavController().navigate(R.id.action_dashboardFragment_to_categoryDetailsFragment, bundle)
    }

}