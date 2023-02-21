package com.example.wordle;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author: Christopher Christiansen
 * @description: The LeaderBoardFragment is responsible for getting the top
 * players information from the server and displaying that information.
 *
 */

public class LeaderBoardFragment extends androidx.fragment.app.DialogFragment {

    public Activity containerActivity = null;

    public LeaderBoardFragment() {

    }
    /**
     * Sets the container Activity for this fragment.
     *
     * @param containerActivity The container Activity for this fragment.
     */
    public void setContainerActivity(Activity containerActivity) {
        this.containerActivity = containerActivity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_leader_board, container, false);

        v.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });
        ListView lv = v.findViewById(R.id.rankList);
        ArrayList<JSONObject> List = new ArrayList<>();

        Bundle ResultsBundle = getArguments();
        if(ResultsBundle != null){
            try {
                JSONArray arr= new JSONArray(ResultsBundle.getString(MainActivity.QUERY_RESULT));
                for (int i = 0; i < arr.length(); i++){
                    JSONObject temp = arr.getJSONObject(i);
                    List.add(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ListAdapter adapter = new ListAdapter(getContext(), R.layout.rank_item, List);
        lv.setAdapter(adapter);

        return v;
    }

    public class ListAdapter extends ArrayAdapter<JSONObject> {
        Context context;
        int resource;
        ArrayList<JSONObject> list;


        public ListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<JSONObject> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            this.list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(resource, parent, false);
            }
            else
            {
                view = convertView;
            }

            // Here
            JSONObject item = list.get(position);
            String name = "";
            int totalPlays = 0;
            int totalWins = 0;
            int maxStreak = 0;
            double percentage = 0;
            try {
                name = item.getString("ID");
                totalPlays = item.getInt("totalPlays");
                totalWins = item.getInt("totalWins");
                maxStreak = item.getInt("maxStreak");
                name = item.getString("Name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (totalPlays != 0){
                percentage = (double) totalWins / totalPlays;
            }

            percentage *= 100;

            TextView nameView = view.findViewById(R.id.name_view);
            nameView.setText(name);

            TextView rankView = view.findViewById(R.id.rank_view);
            rankView.setText("" + (position + 1));

            TextView streakView = view.findViewById(R.id.current_streak_view);
            streakView.setText("" + maxStreak);

            TextView gamesView = view.findViewById(R.id.win_stat_view);
            gamesView.setText("" + totalPlays);

            TextView winPercentageView = view.findViewById(R.id.win_percentage_view);
            winPercentageView.setText("" + (int)percentage);

            return view;
        }
    }
}