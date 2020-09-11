package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.shushme.provider.PlaceContentProvider;
import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 111;
    private static final int PLACE_PICKER_REQUEST = 1;

    // Member variables
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // TODO (4) Create a GoogleApiClient with the LocationServices API and GEO_DATA_API
        GoogleApiClient mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Api client Connection Successful");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Api client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Api client connection Failed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckBox checkBox = findViewById(R.id.mCheckBox);
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkBox.setChecked(false);
        }
        else {
            checkBox.setChecked(true);
            checkBox.setEnabled(false);
        }
    }

    public void onAddPlaceButtonClicked(View view){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_LONG).show();

            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent = builder.build(this);
                startActivityForResult(intent, PLACE_PICKER_REQUEST);
            }catch (GooglePlayServicesRepairableException e) {
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
            }
        }
    }

    protected void onActivityresult(int requestcode, int resultcode, Intent data){
        if (requestcode == PLACE_PICKER_REQUEST && resultcode == RESULT_OK){
            Place place = PlacePicker.getPlace(this,data);
            if (place == null){
                Log.d(TAG, "NO PLACES SELETED");
                return;
            }
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeID);
            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);
        }
    }
    public void onLocationPermissionClicked(View view){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
    }
    // TODO (5) Override onConnected, onConnectionSuspended and onConnectionFailed for GoogleApiClient
    // TODO (7) Override onResume and inside it initialize the location permissions checkbox
    // TODO (8) Implement onLocationPermissionClicked to handle the CheckBox click event
    // TODO (9) Implement the Add Place Button click event to show  a toast message with the permission status

}
