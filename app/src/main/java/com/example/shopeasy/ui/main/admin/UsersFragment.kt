package com.example.shopeasy.ui.main.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.User
import com.example.shopeasy.ui.main.users.UserReadOnlyAdapter

class UsersFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView

    // Lista de 4 usuarios genéricos para visualización
    private val userList = listOf(
        User(1, "Juan Jimenez 1", "usuario1@shopeasy.com", "Cliente", R.drawable.ic_person),
        User(2, "Andres Martinez 2", "usuario2@shopeasy.com", "Soporte Técnico", R.drawable.ic_person),
        User(3, "Diego Farfan 3", "usuario3@shopeasy.com", "Ventas", R.drawable.ic_person),
        User(4, "Ana Lopez 4", "usuario4@shopeasy.com", "Logística", R.drawable.ic_person)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usamos fragment_users que es el layout diseñado para solo lectura
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsers = view.findViewById(R.id.rv_users_list)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Utilizamos el adaptador de solo lectura
        val adapter = UserReadOnlyAdapter(userList)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter
    }
}