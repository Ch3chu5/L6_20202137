package com.example.l6_20202137;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.l6_20202137.models.Egreso;
import com.example.l6_20202137.models.Ingreso;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResumenFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private Button btnSeleccionarMes;
    private TextView tvMesSeleccionado;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    
    private Calendar calendar;
    private SimpleDateFormat monthYearFormat;
    
    private double totalIngresos = 0;
    private double totalEgresos = 0;

    public ResumenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        btnSeleccionarMes = view.findViewById(R.id.btnSeleccionarMes);
        tvMesSeleccionado = view.findViewById(R.id.tvMesSeleccionado);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(getContext(), "Error: No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            return view;
        }
        
        calendar = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        actualizarTextViewMes();
        
        btnSeleccionarMes.setOnClickListener(v -> mostrarSelectorMes());
        
        cargarDatosDelMes();
        
        return view;
    }

    private void actualizarTextViewMes() {
        tvMesSeleccionado.setText(monthYearFormat.format(calendar.getTime()));
    }
    
    private void mostrarSelectorMes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(view);

        NumberPicker monthPicker = view.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = view.findViewById(R.id.yearPicker);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);

        String[] meses = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                                     "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        monthPicker.setDisplayedValues(meses);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 5);
        yearPicker.setMaxValue(currentYear + 5);

        monthPicker.setValue(calendar.get(Calendar.MONTH));
        yearPicker.setValue(calendar.get(Calendar.YEAR));

        AlertDialog dialog = builder.create();

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            calendar.set(Calendar.YEAR, yearPicker.getValue());
            calendar.set(Calendar.MONTH, monthPicker.getValue());
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Primer día del mes

            actualizarTextViewMes();
            cargarDatosDelMes();

            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void cargarDatosDelMes() {
        Toast.makeText(getContext(), "Cargando datos...", Toast.LENGTH_SHORT).show();
        
        Calendar inicioMes = (Calendar) calendar.clone();
        inicioMes.set(Calendar.DAY_OF_MONTH, 1);
        inicioMes.set(Calendar.HOUR_OF_DAY, 0);
        inicioMes.set(Calendar.MINUTE, 0);
        inicioMes.set(Calendar.SECOND, 0);
        
        Calendar finMes = (Calendar) calendar.clone();
        finMes.set(Calendar.DAY_OF_MONTH, finMes.getActualMaximum(Calendar.DAY_OF_MONTH));
        finMes.set(Calendar.HOUR_OF_DAY, 23);
        finMes.set(Calendar.MINUTE, 59);
        finMes.set(Calendar.SECOND, 59);
        
        Date fechaInicio = inicioMes.getTime();
        Date fechaFin = finMes.getTime();
        
        totalIngresos = 0;
        totalEgresos = 0;
        
        db.collection("usuarios").document(userId)
            .collection("ingresos")
            .whereGreaterThanOrEqualTo("fecha", fechaInicio)
            .whereLessThanOrEqualTo("fecha", fechaFin)
            .get()
            .addOnSuccessListener(querySnapshotIngresos -> {
                for (DocumentSnapshot document : querySnapshotIngresos) {
                    Ingreso ingreso = document.toObject(Ingreso.class);
                    if (ingreso != null) {
                        totalIngresos += ingreso.getMonto();
                    }
                }
                
                db.collection("usuarios").document(userId)
                    .collection("egresos")
                    .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                    .whereLessThanOrEqualTo("fecha", fechaFin)
                    .get()
                    .addOnSuccessListener(querySnapshotEgresos -> {
                        for (DocumentSnapshot document : querySnapshotEgresos) {
                            Egreso egreso = document.toObject(Egreso.class);
                            if (egreso != null) {
                                totalEgresos += egreso.getMonto();
                            }
                        }
                        
                        actualizarGraficos();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), 
                                "Error al cargar los egresos: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), 
                        "Error al cargar los ingresos: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
            });
    }
    
    private void actualizarGraficos() {
        configurarGraficoPastel();
        configurarGraficoBarras();
    }
    
    private void configurarGraficoPastel() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Distribución de Finanzas");
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        
        List<PieEntry> entries = new ArrayList<>();
        
        if (totalIngresos == 0 && totalEgresos == 0) {
            entries.add(new PieEntry(1f, "Sin datos"));
        } else {
            // Calcular porcentaje de egresos respecto al total
            float total = (float) (totalIngresos + totalEgresos);
            
            if (totalIngresos > 0) {
                entries.add(new PieEntry((float) totalIngresos, "Ingresos"));
            }
            
            if (totalEgresos > 0) {
                entries.add(new PieEntry((float) totalEgresos, "Egresos"));
            }
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(46, 125, 50)); // Verde para ingresos
        colors.add(Color.rgb(198, 40, 40)); // Rojo para egresos
        colors.add(Color.LTGRAY); // Gris para sin datos
        dataSet.setColors(colors);
        
        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        
        data.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));
        
        pieChart.setData(data);
        pieChart.highlightValues(null);
        
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        
        pieChart.invalidate();
    }
    
    private void configurarGraficoBarras() {
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        final String[] etiquetas = new String[]{"Ingresos", "Egresos", "Consolidado"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisRight().setEnabled(false);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) totalIngresos)); // Ingresos
        entries.add(new BarEntry(1f, (float) totalEgresos)); // Egresos
        entries.add(new BarEntry(2f, (float) (totalIngresos - totalEgresos))); // Consolidado (balance neto)
        
        BarDataSet dataSet = new BarDataSet(entries, "Finanzas del mes");
        
        int[] colores = new int[]{
                Color.rgb(46, 125, 50), // Verde para ingresos
                Color.rgb(198, 40, 40), // Rojo para egresos
                Color.rgb(25, 118, 210) // Azul para consolidado
        };
        dataSet.setColors(colores);
        
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        data.setValueTextSize(11f);
        
        barChart.setData(data);
        barChart.setFitBars(true);
        
        Legend l = barChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        
        barChart.invalidate();
    }
}
