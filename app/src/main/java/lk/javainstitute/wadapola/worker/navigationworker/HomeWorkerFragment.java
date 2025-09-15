package lk.javainstitute.wadapola.worker.navigationworker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomerData;

public class HomeWorkerFragment extends Fragment {

    private BarChart barChart;
    private List<String> dateLabels = new ArrayList<>(); // Store dates for X-axis labels

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_worker, container, false);
        barChart = view.findViewById(R.id.bar_chart1);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String workerId = customerData.getId(); // Replace worker ID

        db.collection("payments")
                .whereEqualTo("worker_id", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BarEntry> barEntries = new ArrayList<>();
                        List<String> dateLabels = new ArrayList<>();
                        int index = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double price = Double.parseDouble(document.getString("price"));
                            double workHours = Double.parseDouble(document.getString("work_hors"));
                            double totalPayment = workHours * price;

                            Timestamp timestamp = document.getTimestamp("date_time");
                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                                String formattedDate = sdf.format(date);

                                dateLabels.add(formattedDate);
                                barEntries.add(new BarEntry(index++, (float) totalPayment)); // Use index for X values
                            }
                        }

                        showBarChart(barEntries, dateLabels);
                    }
                });

        TextView textViewTotalPrice = view.findViewById(R.id.textViewWorkerEarnings),
                textViewBookingCount = view.findViewById(R.id.worker_textViewBookingCount),
                textViewBookings = view.findViewById(R.id.textViewWorkerBookings1);

        db.collection("payments")
                .whereEqualTo("worker_id", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BarEntry> barEntries = new ArrayList<>();
                        List<String> dateLabels = new ArrayList<>();
                        int index = 0;
                        double totalEarnings = 0.0; // Track total earnings
                        int bookingCount = 0; // Track booking count

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double price = Double.parseDouble(document.getString("price"));
                            double workHours = Double.parseDouble(document.getString("work_hors"));
                            double totalPayment = workHours * price;

                            // Add payment to total earnings
                            totalEarnings += totalPayment;
                            bookingCount++; // Increase booking count

                            Timestamp timestamp = document.getTimestamp("date_time");
                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                                String formattedDate = sdf.format(date);

                                dateLabels.add(formattedDate);
                                barEntries.add(new BarEntry(index++, (float) totalPayment)); // Use index for X values
                            }
                        }

                        // Show Bar Chart
                        showBarChart(barEntries, dateLabels);

                        // **Update TextViews**
                        textViewTotalPrice.setText(String.format("Total Earnings: LKR %.2f", totalEarnings));
                        textViewBookingCount.setText(String.format("Total Bookings: %d", bookingCount));
                    }
                });

        db.collection("booking")
                .whereEqualTo("worker_id",workerId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            int size = task.getResult().size();
                            textViewBookings.setText(String.valueOf(size));
                        }
                    }
                });




        return view;
    }

    private void showBarChart(List<BarEntry> barEntries, List<String> dateLabels) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "Total Payment");
        barDataSet.setValueTextSize(14f);
        barDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        barDataSet.setValueTextColor(getContext().getColor(R.color.c1));

        // Generate random colors for each bar
        ArrayList<Integer> colors = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < barEntries.size(); i++) {
            int color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            colors.add(color);
        }
        barDataSet.setColors(colors);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f); // Adjust bar width for better spacing

        // Configure Bar Chart
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.animateY(1500, Easing.EaseInOutQuad);
        barChart.getLegend().setEnabled(false); // Hide legend for cleaner UI
        barChart.setExtraBottomOffset(10f); // Adjust space for X labels

        // Set Data
        barChart.setData(barData);

        // Configure X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-30);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(getContext().getColor(R.color.c4));
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);

        // **Format X-Axis Labels with Dates**
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dateLabels.size()) {
                    return dateLabels.get(index); // Ensure index is within bounds
                } else {
                    return "";
                }
            }
        });

        // Configure Y-Axis (Left)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.GRAY);
        leftAxis.setTextColor(getContext().getColor(R.color.c3));
        leftAxis.setTextSize(12f);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD);
        leftAxis.setAxisMinimum(0f); // Ensure Y-axis starts from 0

        // Hide right Y-axis
        barChart.getAxisRight().setEnabled(false);

        // Refresh chart
        barChart.invalidate();
    }

}
