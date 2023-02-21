package com.example.wordle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONObject;

import java.util.HashMap;


/**
 *
 * @author: Rey Sanayei
 * @description: The Stats Fragment's java code. Responsible for displaying the user's stats
 * including total plays, wins, max and current streaks, and the guess-based breakdown of past wins.
 * This fragment also enables the user to share their win, and play another game if the current
 * game is over.
 *
 */
public class StatsFragment extends androidx.fragment.app.DialogFragment {

    public Activity containerActivity = null;
    private Player player;
    private ImageView closeBtn;
    private TextView playedStat;
    private TextView winStat;
    private TextView curStreakStat;
    private TextView maxStreakStat;

    private TextView wins1;
    private TextView wins2;
    private TextView wins3;
    private TextView wins4;
    private TextView wins5;
    private TextView wins6;

    private Button replayBttn;
    private Button shareBttn;

    private Boolean isOver;
    private Boolean didWin;
    private WordleModel model;



    /**
     * Required empty public constructor
     */
    public StatsFragment() {
    }

    /**
     * Sets the container Activity for this fragment.
     *
     * @param containerActivity The container Activity for this fragment.
     */
    public void setContainerActivity(Activity containerActivity) {
        this.containerActivity = containerActivity;
    }

    /**
     * The method called on the creation of the fragment's instance. This method also defines
     * the OnClick event-handling for the readOn Button.
     *
     * @param inflater           The LayoutInflater
     * @param container          The ViewGroup that contains this fragment
     * @param savedInstanceState The Bundle that holds saved information for recovering this instance
     * @return View The view that gets inflated is returned.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle boolsBundle = getArguments();
        if(boolsBundle != null){
            isOver = boolsBundle.getBoolean(MainActivity.REQUEST_IS_OVER,false);
            didWin =boolsBundle.getBoolean(MainActivity.REQUEST_DID_WIN,false);
        }
        else{
            isOver = false;
            didWin = false;
        }
        player = ((MainActivity)containerActivity).getPlayer();
        model = ((MainActivity)containerActivity).getModel();
        View v = inflater.inflate(R.layout.fragment_stats, container, false);
        closeBtn = v.findViewById(R.id.close_button);
        closeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismissAllowingStateLoss();
            }
        });
        playedStat = v.findViewById(R.id.played_stat);
        playedStat.setText(String.valueOf(player.getTotalPlays()));

        winStat = v.findViewById(R.id.win_stat);
        int winPercent = 0;
        if(player.getTotalPlays() != 0)
            winPercent =(int)(((double)player.getTotalWins()/(double)player.getTotalPlays())*100.0);
        winStat.setText(String.valueOf(winPercent));

        curStreakStat = v.findViewById(R.id.cur_streak_stat);
        curStreakStat.setText(String.valueOf(player.getCurStreak()));

        maxStreakStat = v.findViewById(R.id.max_streak_stat);
        maxStreakStat.setText(String.valueOf(player.getMaxStreak()));

        HashMap<Integer, Integer> history = player.getGuessHistory();
        wins1 = v.findViewById(R.id.wins_1_data);
        wins1.setText(String.valueOf(history.get(1)));
        wins2 = v.findViewById(R.id.wins_2_data);
        wins2.setText(String.valueOf(history.get(2)));
        wins3 = v.findViewById(R.id.wins_3_data);
        wins3.setText(String.valueOf(history.get(3)));
        wins4 = v.findViewById(R.id.wins_4_data);
        wins4.setText(String.valueOf(history.get(4)));
        wins5 = v.findViewById(R.id.wins_5_data);
        wins5.setText(String.valueOf(history.get(5)));
        wins6 = v.findViewById(R.id.wins_6_data);
        wins6.setText(String.valueOf(history.get(6)));

        replayBttn = v.findViewById(R.id.replay_btn);
        replayBttn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity)containerActivity).setModel(new WordleModel(v));
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStackImmediate();
                }
                ((MainActivity)containerActivity).recreate();
                dismissAllowingStateLoss();
            }
        });
        if(!isOver){
            replayBttn.setEnabled(false);
            replayBttn.setAlpha(0);
        }

        shareBttn = v.findViewById(R.id.share_btn);
        shareBttn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String res = resultStringBuilder();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, res);
                intent.setType("text/plain");
                startActivity(intent.createChooser(intent,"Share"));
            }
        });
        // does it work with loss and win? previously !didWin
        if(!isOver){
            shareBttn.setEnabled(false);
            shareBttn.setAlpha(0);
        }
        return v;
    }

    public String resultStringBuilder(){
        String res = "";
        int guessCount = 0;
        Guess[] guessArr = model.getProgress();
        for (int i = 0; i <guessArr.length; i++) {
            if (guessArr[i] != null)
                guessCount++;
        }
        res += "Wordle "+ String.valueOf(player.getTotalPlays()) + " " + String.valueOf(guessCount)+
                "/6\n";
        for (int i = 0; i <guessCount; i++) {

            for (int j = 0; j < 5; j++) {
                INDEX_RESULT cur = guessArr[i].getIndices()[j];
                if (cur == INDEX_RESULT.INCORRECT)
                    res += new String(Character.toChars(0x2B1C));
                else if (cur == INDEX_RESULT.CORRECT_WRONG_INDEX)
                    res += new String(Character.toChars(0x1F7E8));
                else if (cur == INDEX_RESULT.CORRECT)
                    res += new String(Character.toChars(0x1F7E9));
            }
            if(i != guessCount - 1)
                res +="\n";
        }
        return res;

    }


}