package com.vinwil.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.vinwil.R;
import com.vinwil.data.DataManager;
import com.vinwil.data.model.Stop;
import com.vinwil.internet.Internet;
import com.vinwil.internet.URLs;
import com.vinwil.userinterface.recycleradapter.SearchDestinationRecyclerAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SearchDestinationActivity extends AppCompatActivity {

    private FloatingSearchView fsvSearchDestination;
    private SwipeRefreshLayout srlSearchDestination;
    private RecyclerView rvSearchDestination;

    private DataManager manager;
    private RequestQueue queue;
    private List<Stop> stops;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_destination);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        fsvSearchDestination = findViewById(R.id.fsv_search_destination);
        srlSearchDestination = findViewById(R.id.srl_search_destination);
        rvSearchDestination = findViewById(R.id.rv_search_destination);

        srlSearchDestination.setColorSchemeResources(R.color.color_accent);

        manager = new DataManager(SearchDestinationActivity.this);
        stops = new ArrayList<>();
        rvSearchDestination.setLayoutManager(new LinearLayoutManager(SearchDestinationActivity.this));
        rvSearchDestination.setAdapter(new SearchDestinationRecyclerAdapter(SearchDestinationActivity.this, stops));

        setSearchDestination(URLs.URL_STOPS);

        fsvSearchDestination.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (newQuery.length() > 1) {
                    setSearchDestination(URLs.URL_STOPS.concat("?search=").concat(newQuery));
                } else {
                    setSearchDestination(URLs.URL_STOPS);
                }
            }
        });

        srlSearchDestination.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setSearchDestination(URLs.URL_STOPS);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void setSearchDestination(String url) {
        srlSearchDestination.setRefreshing(true);
        queue = Volley.newRequestQueue(SearchDestinationActivity.this);

        if (!Internet.isNetworkAvailable(SearchDestinationActivity.this)) {
            Toast.makeText(SearchDestinationActivity.this, R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray routesData) {
                        stops.clear();
                        rvSearchDestination.getAdapter().notifyDataSetChanged();
                        try {
                            if (routesData.length() > 0) {
                                for (int i = 0; i < routesData.length(); i++) {
                                    Stop stop = manager.getStop(routesData.getJSONObject(i));
                                    if (stop != null) {
                                        stops.add(stop);
                                        rvSearchDestination.getAdapter().notifyDataSetChanged();
                                    }
                                }
                                if (stops.size() == 0) {
                                    throw new Exception();
                                }
                            } else {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            // An error occurred
                        } finally {
                            srlSearchDestination.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        srlSearchDestination.setRefreshing(false);
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 10, 10));
        queue.add(request);
    }

}
