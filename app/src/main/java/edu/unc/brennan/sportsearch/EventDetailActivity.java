package edu.unc.brennan.sportsearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;

/**
 * An activity representing a single Event detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link EventListActivity}.
 */
public class EventDetailActivity extends AppCompatActivity {
    static boolean clicked = false;
    EventDetailFragment fragment;
    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

//        if(clicked){
//            fab.setImageResource(R.drawable.done);
//        }
        FloatingActionButton im = (FloatingActionButton) findViewById(R.id.im);
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EventDetailActivity.this, ConversationActivity.class);
                intent.putExtra(ConversationUIService.TAKE_ORDER,true);
                intent.putExtra(ConversationUIService.GROUP_ID, fragment.getKey());      //Pass group id here.
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("change");
                if(!clicked) {
                    fab.setImageResource(R.drawable.done);
                    System.out.println("Incrementing");
                    fragment.increment();
                    System.out.println("Done incrementing");
                    Toast.makeText(EventDetailActivity.this, "Joined game",
                            Toast.LENGTH_SHORT).show();
                }else{
                    fab.setImageResource(R.drawable.add);
                    fragment.decrement();
                    Toast.makeText(EventDetailActivity.this, "Left game",
                            Toast.LENGTH_SHORT).show();
                }
                clicked=!clicked;
                //Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(EventDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(EventDetailFragment.ARG_ITEM_ID));
            fragment = new EventDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.event_detail_container, fragment)
                    .commit();
        }
    }

    public void map(View v){
        Intent x = new Intent(EventDetailActivity.this, MapsActivity.class);
        startActivity(x);
    }

    public void events(View v){
        NavUtils.navigateUpTo(this, new Intent(this, EventListActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, EventListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
