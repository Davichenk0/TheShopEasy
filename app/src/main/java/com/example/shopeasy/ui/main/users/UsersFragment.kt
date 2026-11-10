package com.example.shopeasy.ui.main.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.User

class UsersFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView

    // 3 Perfiles estáticos que no se pueden modificar
    private val staticUserList = listOf(
        User(101, "Carlos Ruiz", "carlos.ruiz@shopeasy.com", "Soporte Técnico", R.drawable.ic_person),
        User(102, "Ana Beltrán", "ana.beltran@shopeasy.com", "Ventas", R.drawable.ic_person),
        User(103, "Luis Méndez", "luis.mendez@shopeasy.com", "Logística", R.drawable.ic_person)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsers = view.findViewById(R.id.rv_users_list)
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val adapter = UserReadOnlyAdapter(staticUserList)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter
    }
}