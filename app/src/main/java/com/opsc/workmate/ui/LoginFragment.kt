package com.opsc.workmate.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.Navigation
import com.opsc.workmate.MainActivity
import com.opsc.workmate.R
import com.opsc.workmate.data.Category
import com.opsc.workmate.data.Entry
import com.opsc.workmate.data.Global
import java.util.logging.Logger.global

class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        //---- Navigate to register screen ----
        // Find the TextView by ID
        val txtSignUp: TextView = view.findViewById(R.id.txtSignUp)

        // Set onClickListener for the TextView
        txtSignUp.setOnClickListener {
            // Get the NavController
            val navController = Navigation.findNavController(view)

            // Navigate to the registerFragment
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        //---- Login button ----
        //Find the button by ID
        val btnLogin: Button = view.findViewById(R.id.btnLogin)

        // Set onClickListener for button
        btnLogin.setOnClickListener {
            // Perform login validation
            val isValid = validateLogin()

            if (isValid) {
                // Navigate to MainActivity
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Display an error message or handle the invalid login case
                Toast.makeText(activity, "Invalid login credentials", Toast.LENGTH_SHORT).show()
            }
        }

        val imgIcon : ImageView = view.findViewById(R.id.imgIcon)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.workmate_logo)
        imgIcon.setImageDrawable(drawable)

        return view
    }

    private fun validateLogin(): Boolean {
        val txtUsername: EditText = requireView().findViewById(R.id.txtUsername)
        val txtPassword: EditText = requireView().findViewById(R.id.txtPassword)

        val username = txtUsername.text.toString()
        val password = txtPassword.text.toString()

        val user = Global.users.find { it.username == username && it.password == password }
        if (user != null) {
            //Success
            Global.currentUser = user
            filterLists()
            return true
        } else {
            //Failure
            Toast.makeText(requireContext(), "Invalid username or password", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun filterLists() {
        //Filter global lists according to logged in user, other user's data is not needed
        //Other user's data gets repopulated each time app opens/LoginActivity

        //Code Attribution
        //The below code was derived from StackOverflow
        //https://stackoverflow.com/questions/70252668/filter-a-list-of-object
        //Erjon
        //https://stackoverflow.com/users/6310948/erjon

        val entries = Global.entries
        val filteredEntries: MutableList<Entry> = mutableListOf()
        entries.forEach { entry ->
            if (entry.username.equals(Global.currentUser!!.username, ignoreCase = true))
                filteredEntries.add(entry) }
        Global.entries = filteredEntries

        val categories = Global.categories
        val filteredCategories: MutableList<Category> = mutableListOf()
        categories.forEach { category ->
            if (category.username.equals(Global.currentUser!!.username, ignoreCase = true))
                filteredCategories.add(category) }
        Global.categories = filteredCategories
    }
}