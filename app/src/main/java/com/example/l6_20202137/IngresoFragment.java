package com.example.l6_20202137;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20202137.adapters.IngresoAdapter;
import com.example.l6_20202137.models.Ingreso;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class IngresoFragment extends Fragment implements IngresoAdapter.OnIngresoListener {

    private RecyclerView recyclerViewIngresos;
    private IngresoAdapter adapter;
    private List<Ingreso> listaIngresos;
    private FloatingActionButton fabAgregarIngreso;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    public IngresoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingreso, container, false);

        recyclerViewIngresos = view.findViewById(R.id.recyclerViewIngresos);
        fabAgregarIngreso = view.findViewById(R.id.fabAgregarIngreso);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(getContext(), "Error: No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Configurar RecyclerView
        listaIngresos = new ArrayList<>();
        adapter = new IngresoAdapter(listaIngresos, this);
        recyclerViewIngresos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngresos.setAdapter(adapter);

        fabAgregarIngreso.setOnClickListener(v -> {
            mostrarDialogoAgregarIngreso();
        });

        cargarIngresos();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void cargarIngresos() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Cargando ingresos...", Toast.LENGTH_SHORT).show();
        }

        db.collection("usuarios").document(userId)
            .collection("ingresos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                listaIngresos.clear();

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Ingreso ingreso = document.toObject(Ingreso.class);
                    if (ingreso != null) {
                        ingreso.setId(document.getId());
                        listaIngresos.add(ingreso);
                    }
                }

                adapter.notifyDataSetChanged();

                if (listaIngresos.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "No hay ingresos registrados", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar los ingresos: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
    }

    private void mostrarDialogoAgregarIngreso() {
        IngresoDialogFragment dialogFragment = new IngresoDialogFragment();
        dialogFragment.setOnIngresoGuardadoListener(() -> cargarIngresos()); // Recargar la lista después de guardar
        dialogFragment.show(getParentFragmentManager(), "IngresoDialogFragment");
    }

    @Override
    public void onIngresoClick(Ingreso ingreso) {
        Toast.makeText(getContext(), "Ingreso: " + ingreso.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditarClick(Ingreso ingreso) {
        IngresoDialogFragment dialogFragment = new IngresoDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("ingreso", ingreso);
        dialogFragment.setArguments(args);
        dialogFragment.setOnIngresoGuardadoListener(() -> cargarIngresos()); // Recargar la lista después de editar
        dialogFragment.show(getParentFragmentManager(), "IngresoDialogFragment");
    }

    @Override
    public void onEliminarClick(Ingreso ingreso) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Ingreso")
                .setMessage("¿Estás seguro de que quieres eliminar este ingreso?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarIngreso(ingreso);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarIngreso(Ingreso ingreso) {
        db.collection("usuarios").document(userId)
            .collection("ingresos")
            .document(ingreso.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Ingreso eliminado correctamente", Toast.LENGTH_SHORT).show();
                cargarIngresos();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error al eliminar el ingreso: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
    }

    public interface OnIngresoGuardadoListener {
        void onIngresoGuardado();
    }
}
