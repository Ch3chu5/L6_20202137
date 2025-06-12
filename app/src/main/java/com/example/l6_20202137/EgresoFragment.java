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

import com.example.l6_20202137.adapters.EgresoAdapter;
import com.example.l6_20202137.models.Egreso;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EgresoFragment extends Fragment implements EgresoAdapter.OnEgresoListener {

    private RecyclerView recyclerViewEgresos;
    private EgresoAdapter adapter;
    private List<Egreso> listaEgresos;
    private FloatingActionButton fabAgregarEgreso;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    public EgresoFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_egreso, container, false);

        recyclerViewEgresos = view.findViewById(R.id.recyclerViewEgresos);
        fabAgregarEgreso = view.findViewById(R.id.fabAgregarEgreso);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(getContext(), "Error: No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            return view;
        }

        listaEgresos = new ArrayList<>();
        adapter = new EgresoAdapter(listaEgresos, this);
        recyclerViewEgresos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEgresos.setAdapter(adapter);

        fabAgregarEgreso.setOnClickListener(v -> {
            mostrarDialogoAgregarEgreso();
        });

        cargarEgresos();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void cargarEgresos() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Cargando egresos...", Toast.LENGTH_SHORT).show();
        }

        db.collection("usuarios").document(userId)
            .collection("egresos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                listaEgresos.clear();

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Egreso egreso = document.toObject(Egreso.class);
                    if (egreso != null) {
                        egreso.setId(document.getId());
                        listaEgresos.add(egreso);
                    }
                }

                adapter.notifyDataSetChanged();

                if (listaEgresos.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "No hay egresos registrados", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar los egresos: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
    }

    private void mostrarDialogoAgregarEgreso() {
        EgresoDialogFragment dialogFragment = new EgresoDialogFragment();
        dialogFragment.setOnEgresoGuardadoListener(() -> cargarEgresos()); // Recargar la lista después de guardar
        dialogFragment.show(getParentFragmentManager(), "EgresoDialogFragment");
    }

    @Override
    public void onEgresoClick(Egreso egreso) {
        Toast.makeText(getContext(), "Egreso: " + egreso.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditarClick(Egreso egreso) {
        EgresoDialogFragment dialogFragment = new EgresoDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("egreso", egreso);
        dialogFragment.setArguments(args);
        dialogFragment.setOnEgresoGuardadoListener(() -> cargarEgresos()); // Recargar la lista después de editar
        dialogFragment.show(getParentFragmentManager(), "EgresoDialogFragment");
    }

    @Override
    public void onEliminarClick(Egreso egreso) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Egreso")
                .setMessage("¿Estás seguro de que quieres eliminar este egreso?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarEgreso(egreso);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarEgreso(Egreso egreso) {
        db.collection("usuarios").document(userId)
            .collection("egresos")
            .document(egreso.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Egreso eliminado correctamente", Toast.LENGTH_SHORT).show();
                cargarEgresos(); // Recargar la lista después de eliminar
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error al eliminar el egreso: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
    }

    public interface OnEgresoGuardadoListener {
        void onEgresoGuardado();
    }
}