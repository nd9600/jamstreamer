package com.leokomarov.jamstreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.leokomarov.jamstreamer.home.HomeController;
import com.leokomarov.jamstreamer.util.ComplexPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ActionBarProvider {

    private Router router;

    @BindView(R.id.controller_container)
    ViewGroup container;

    public static ComplexPreferences trackPreferences;
    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.trackPreferences = ComplexPreferences.getComplexPreferences(this,
                getString(R.string.trackPreferences), Context.MODE_PRIVATE);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //attaches a router to this activity
        //router's controllers are hosted in the container
        //savedInstanceState is used to restore the router's state if possible
        router = Conductor.attachRouter(this, container, savedInstanceState);

        //sets the root controller if it doesn't already exist
        //.with() returns a new RouterTransaction
        if (! router.hasRootController()){
            router.setRoot(RouterTransaction.with(new HomeController()));
        }
    }

    @Override
    public void onBackPressed(){
        if (! router.handleBack()){
            super.onBackPressed();
        }
    }
}
