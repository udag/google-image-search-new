package udit.com.google_image_search.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.kanikash.gridimagesearch.R;
import com.kanikash.gridimagesearch.adapters.ImageResultsAdapter;
import com.kanikash.gridimagesearch.models.EndlessScrollListener;
import com.kanikash.gridimagesearch.models.ImageResult;
import com.kanikash.gridimagesearch.models.SearchFilterFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SearchActivity extends ActionBarActivity {

    private GridView gvResults;
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;
    private String query;
    private String imageSizeFilter = "any";
    private String colorFilter = "any";
    private String imageType = "any";
    private String siteFilter = "";
    private SearchFilterFragment searchFilterFm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        handleIntent(getIntent());
        setupViews();
        //Creates the data source
        imageResults = new ArrayList<ImageResult>();
        //Attaches the data source to the adapter
        aImageResults = new ImageResultsAdapter(this, imageResults);
        //Link the adapter to adapterView
        gvResults.setAdapter(aImageResults);
    }

    private void setupViews() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Launch the image display activity
                // Creating an intent
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                // Get the image data to display
                ImageResult result = imageResults.get(position);
                // Pass the data to launch display activity
                i.putExtra("result", result);
                // Launch the new intent
                startActivity(i);
            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener(4, 0) {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                customLoadMoreDataFromApi(page, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            customLoadMoreDataFromApi(0, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Display the modal dialog
            FragmentManager fm = getSupportFragmentManager();
            searchFilterFm = SearchFilterFragment.newInstance(this.imageSizeFilter, this.colorFilter, this.imageType, this.siteFilter);
            searchFilterFm.show(fm, "fragment_search_filter");
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    public void customLoadMoreDataFromApi(int offset, final boolean isNewQuery) {
        AsyncHttpClient client = new AsyncHttpClient();
        String searchUrl = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + query + "&rsz=8" + "&start=" + offset + "&imgsz=" + imageSizeFilter + "&imgcolor=" + colorFilter + "&imgtype=" + imageType + "&as_sitesearch=" + siteFilter;
        if(!query.isEmpty()) {
            client.get(searchUrl, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //super.onSuccess(statusCode, headers, response);
                    try {
                        JSONArray imageResultsJson = response.getJSONObject("responseData").getJSONArray("results");
                        if (isNewQuery) {
                            imageResults.clear(); //clear the existing images from the array(in case where its a new search)
                        }
                        aImageResults.addAll(ImageResult.fromJSONArray(imageResultsJson));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);

                }
            });
        }
    }

    public void setAdditionalFilter(String sizeFilter, String colorFilter, String typeFiler, String siteFilter) {
        boolean refreshList = false;
        if(sizeFilter != this.imageSizeFilter) {
            this.imageSizeFilter = sizeFilter;
            refreshList = true;
        }
        if(colorFilter != this.colorFilter) {
            this.colorFilter = colorFilter;
            refreshList = true;
        }
        if(typeFiler != this.imageType) {
            this.imageType = typeFiler;
            refreshList = true;
        }
        if(siteFilter != this.siteFilter) {
            this.siteFilter = siteFilter;
            refreshList = true;
        }

        if(refreshList) {
            customLoadMoreDataFromApi(0, true);
        }
    }

}
