package com.example.currency;

import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView currencyListView;
    private EditText searchEditText;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> originalData;
    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencyListView = findViewById(R.id.currencyListView);
        searchEditText = findViewById(R.id.searchEditText);
        String apiKey = BuildConfig.API_KEY;

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        currencyListView.setAdapter(adapter);

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    filterCurrencyList(textView);
                    return true;
                }
                return false;
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                debounceHandler.removeCallbacks(debounceRunnable);

                debounceRunnable = new Runnable() {
                    @Override
                    public void run() {
                        filterCurrencyList(searchEditText);
                    }
                };

                debounceHandler.postDelayed(debounceRunnable, 700);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        new DataLoader(new DataLoader.DataLoadListener() {
            @Override
            public void onDataLoaded(String result) {
                if (result != null) {
                    originalData = Parser.parseXMLData(result);
                    adapter.addAll(originalData);
                    adapter.notifyDataSetChanged();
                }
            }
        }, apiKey).execute();
    }

    public void filterCurrencyList(View view) {
        String searchText = searchEditText.getText().toString().toUpperCase();

        ArrayList<String> filteredList = new ArrayList<>();

        if (originalData != null) {
            for (String currencyRate : originalData) {
                if (currencyRate.toUpperCase().contains(searchText)) {
                    filteredList.add(currencyRate);
                }
            }
        }

        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
