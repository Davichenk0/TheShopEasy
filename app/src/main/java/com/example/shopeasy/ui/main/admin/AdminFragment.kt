package com.example.shopeasy.ui.main.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopeasy.R
import com.example.shopeasy.data.User
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var fabAddUser: FloatingActionButton
    private lateinit var adapter: UserAdapter

    // Lista en memoria para simular el CRUD de usuarios en Admin
    private val userList = mutableListOf(
        User(1, "David Garcia", "david@example.com", "Admin"),
        User(2, "Maria Lopez", "maria@example.com", "User"),
        User(3, "Juan Perez", "juan@example.com", "User")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Usamos el layout de admin que tiene rv_users_admin y fab_add_user_admin
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsers = view.findViewById(R.id.rv_users_admin)
        fabAddUser = view.findViewById(R.id.fab_add_user_admin)

        setupRecyclerView()

        fabAddUser.setOnClickListener {
            showUserDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            userList,
            onEditClick = { user -> showUserDialog(user) },
            onDeleteClick = { user -> deleteUser(user) }
        )
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter
    }

    private fun showUserDialog(user: User?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_email)
        val etRole = dialogView.findViewById<EditText>(R.id.et_role)

        user?.let {
            etName.setText(it.name)
            etEmail.setText(it.email)
            etRole.setText(it.role)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (user == null) "Añadir Usuario" else "Editar Usuario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val role = etRole.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty() && role.isNotEmpty()) {
                    if (user == null) {
                        val newUser = User(userList.size + 1, name, email, role)
                        userList.add(newUser)
                        Toast.makeText(requireContext(), "Usuario añadido", Toast.LENGTH_SHORT).show()
                    } else {
                        user.name = name
                        user.email = email
                        user.role = role
                        Toast.makeText(requireContext(), "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    }
                    adapter.updateList(userList.toList())
                } else {
                    Toast.makeText(requireContext(), "Campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Eliminar a ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                userList.remove(user)
                adapter.updateList(userList.toList())
                Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}