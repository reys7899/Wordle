package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

/**
 *
 * @author: Rey Sanayei & Christopher Christiansen (Only Server-Leaderboard communications)
 * @description: The Main Activity's java code. The Main Activity's java code. Responsible for
 * displaying the Game Board, Keyboard,and the buttons launching the fragments like help, settings,
 * stats, and leaderboard. Also responsible for storing and recovering user progress and game history.
 *
 */
public class MainActivity extends AppCompatActivity {
    private Player player;
    private String[] Permissions;
    private static final String REQUEST_PLAYER = "MY PLAYER";
    private static final String PLAYER_FILE_NAME ="player.txt";
    private static final String MODEL_PROGRESS_FILE_NAME ="progress.txt";
    private static final String REQUEST_MODEL_ANSWER = "IS MODEL ANSWER SAVED?";
    private static final String MODEL_GUESSED_FILE_NAME ="guessed.txt";
    public static final String QUERY_RESULT = "RESULT";
    private WordleModel model;
    // the board positions (starts from 1)
    private int curRow;
    private int curCol;
    // 2D View Array of the game tiles (game board) ids
    private View[][] tileIDArr;
    // View Array of the game keyboard keys' ids
    private View[] keyArr;
    // View Array of the game tile rows' ids
    private LinearLayout[] allParentLLs;
    private boolean isOver;
    private boolean didWin;
    // alert displaying things like wrong guess, a win message, or the right answer if losing.
    private TextView alertText;
    private SharedPreferences sharedPref;
    private Activity curActivity;
    public static final String REQUEST_IS_OVER = "isOver VALUE";
    public static final String REQUEST_DID_WIN = "didWin VALUE";

    /**
     * This function is called on the creation of the activity.
     *It accepts one Bundle parameter, and does not return anything.
     * @param savedInstanceState the saved bundle for the previous state of this instance of activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        curActivity = this;
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        String[] t = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        Permissions = t;
        CheckPermission();

        boolean isPlayerSaved = sharedPref.getBoolean(REQUEST_PLAYER, false);
        // getting saved player info if available
        if (isPlayerSaved) {
            FileInputStream fis = null;
            try {
                fis = this.openFileInput(PLAYER_FILE_NAME);

                ObjectInputStream is = null;
                is = new ObjectInputStream(fis);
                player = (Player) is.readObject();
                is.close();
                fis.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                player = new Player();
            }
        }
        else
            player = new Player();

        // setting the theme according to the player specifications
        if(!player.isThemeLight())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // check if has User Id
        String userId = sharedPref.getString(getString(R.string.saved_user_id_key), getString(R.string.not_there));
        if (userId.equals(getString(R.string.not_there))){
            getNewUserId();
        }

        String modelAnswer = sharedPref.getString(REQUEST_MODEL_ANSWER, null);
        // getting the saved model if available
        if (modelAnswer != null) {
            FileInputStream fis = null;
            INDEX_RESULT[] guessedCharsSaved;
            Guess[] progressSaved;
            try {
                fis = this.openFileInput(MODEL_GUESSED_FILE_NAME);
                ObjectInputStream is = null;
                is = new ObjectInputStream(fis);
                guessedCharsSaved = (INDEX_RESULT[]) is.readObject();
                is.close();
                fis.close();
                fis = this.openFileInput(MODEL_PROGRESS_FILE_NAME);
                is = new ObjectInputStream(fis);
                progressSaved = (Guess[]) is.readObject();
                is.close();
                fis.close();
                model = new WordleModel(findViewById(android.R.id.content).getRootView(),
                        progressSaved, guessedCharsSaved, modelAnswer);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                model = new WordleModel(findViewById(android.R.id.content).getRootView());
            }
        }
        else {
            model = new WordleModel(findViewById(android.R.id.content).getRootView());
        }

        isOver = false;
        curRow = 1;
        curCol = 1;

        alertText = findViewById(R.id.alertText);
        findViewById(R.id.rank_button).setOnClickListener(this::getTopUsers);

        tileIDArr = tileIDArrayBuilder();
        keyArr = keyArrBuilder();
        for (View temp: keyArr) {
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onKeyboardClick(v);
                }
            });
        }

        allParentLLs = new LinearLayout[]{findViewById(R.id.tile_1),
                findViewById(R.id.tile_2), findViewById(R.id.tile_3),
                findViewById(R.id.tile_4), findViewById(R.id.tile_5),
                findViewById(R.id.tile_6)};

        // if there is a saved model progress, update board position
        for (int i = 0; i < model.getProgress().length; i++) {
            if (model.getProgress()[i] != null)
                curRow++;
        }
        // if the current row is past the end limit, game's over
        if (curRow > 6){
            isOver = true;
            didWin= model.getProgress()[5].getIsCorrect();
        }
        else if (model.getProgress()[curRow - 1] != null) {
            if (model.getProgress()[curRow - 1].getIsCorrect()) {
                isOver = true;
                didWin = true;
            }
        }
        // if the current row is not 1, then the board has info, thus update it
        if (curRow != 1){
            updateKeyboard();
            updateBoard();
        }
        if(isOver && (!didWin)){
            String msg = model.getAnswer();
            showFadingText(alertText, msg);

        }

    }
    /**
     * This method is called when the Activity is out of focus
     * @param savedInstanceState the saved bundle for the previous state of this instance of activity
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // getting preferences from a specified file
        super.onSaveInstanceState(savedInstanceState);
        writePlayerToFile(player,this.getBaseContext());
        writeModelInfoToFile(this.getBaseContext());
    }


    /**
     * this method is called when the Activity gets destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        writePlayerToFile(player,this.getBaseContext());
        writeModelInfoToFile(this.getBaseContext());
    }

    /**
     * this method checks to see if the app has the required permissions.
     * If the permissions are not satisfied, the method calls the RequestPermissions method to
     * ask the user for the required permissions.
     */
    private void CheckPermission(){
        boolean result = true;

        for (String s: Permissions){
            if (ContextCompat.checkSelfPermission(this, s) == PackageManager.PERMISSION_DENIED){
                result = false;
            }
        }
        if (!result) {
            RequestPermissions();
        }
    }

    /**
     * This method requests all permission that the app currently does not have from the user.
     * If the user denys any permissions, the function will call the onPermisionDenied method.
     */
    private void RequestPermissions(){
        requestPermissions(Permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean result = true;
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    result = false;
                }
            }

        }
    }

    /**
     * Method for fetching the new user id
     */
    private void getNewUserId(){
        new OnlineCommunication().execute(getString(R.string.saved_user_id_key),getString(R.string.new_id_url), getString(R.string.get_type));
    }

    /**
     * Method for sending the user data to the server
     */
    public void sendUserData(){
        JSONObject postData = new JSONObject();
        try {

            postData.put("ID", sharedPref.getString(getString(R.string.saved_user_id_key), "NA"));
            postData.put("Name", player.getName());
            postData.put("totalPlays", player.getTotalPlays());
            postData.put("totalWins", player.getTotalWins());
            postData.put("maxStreak", player.getMaxStreak());

            new OnlineCommunication().execute(getString(R.string.Send_User_data),getString(R.string.post_score_url), getString(R.string.post_type), postData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for getting the highscorers' info
     * @param v
     */
    private void getTopUsers(View v){
        new OnlineCommunication().execute(getString(R.string.get_highscores),getString(R.string.get_highscores_url), getString(R.string.get_type));
    }


    /**
     * This method sets UserId to the input it receives
     * @param id the preferred user id
     */
    private void setUserId(String id){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_user_id_key), id);
        editor.putString(getString(R.string.saved_username_key), id);
        editor.apply();
        player.setName(id);
        player.setID(id);
    }

    /**
     * Getter method for the Player instance in the activity
     * @return Player  player
     */
    public Player getPlayer(){
        return this.player;
    }

    /**
     * Getter method for the WordleModel instance in the activity
     * @return WordleModel model
     */
    public WordleModel getModel(){
        return this.model;
    }

    /**
     * Setter method for the WordleModel instance in the activity
     * @param model WordleModel to be placed
     */
    public void setModel(WordleModel model){
         this.model = model;
    }

    /**
     * Method for writing the Player instance to local memory
     * @param data the Player to save
     * @param context the Player's context
     */
    private void writePlayerToFile(Player data,Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(PLAYER_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(data);
            os.close();
            fos.close();
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(REQUEST_PLAYER,true);
            editor.apply();
            sendUserData();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Method for writing the WordleModel instance to local memory
     * @param context the WordleModel's context
     */
    private void writeModelInfoToFile(Context context) {
        // writing the progress of the WordleModel
        try {
            FileOutputStream fos = context.openFileOutput(MODEL_PROGRESS_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(model.getProgress());
            os.close();
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed 1: " + e.toString());
        }
        // writing the guesses used for the WordleModel
        try {
            FileOutputStream fos = context.openFileOutput(MODEL_GUESSED_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(model.getGuessedCharacters());
            os.close();
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed 2: " + e.toString());
        }
        // setting the availability of the model in the preferences
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REQUEST_MODEL_ANSWER,model.getAnswer());
        editor.apply();
    }

    /**
     * The click event handler for the game keyboard keys
     * @param v the view getting clicked
     */
    public void onKeyboardClick(View v){
        String toPut = (String) ((Button)v).getText();
        if((!isOver) && curCol <6 && curRow <7){
            ((TextView)tileIDArr[curRow - 1][curCol - 1]).setText(toPut);
            ((TextView)tileIDArr[curRow - 1][curCol - 1]).setBackgroundResource(R.drawable.tile_filled);
            curCol++;
        }
    }

    /**
     * The click event handler for the game keyboard  delete key
     * @param v the view getting clicked
     */
    public void onDelete(View v){
        if((!isOver) && curCol > 1){
            curCol--;
            ((TextView)tileIDArr[curRow - 1][curCol - 1]).setText("");
            ((TextView)tileIDArr[curRow - 1][curCol - 1]).setBackgroundResource(R.drawable.tile_empty);
        }
    }

    /**
     * The click event handler for the game keyboard enter key
     * @param v the view getting clicked
     */
    public void onEnter(View v){
        if(isOver)
            return;
        if( curRow <7){
            LinearLayout curLL = allParentLLs[curRow - 1];
            if (curCol != 6) {
                showFadingText(alertText, "NOT ENOUGH LETTERS");
                // shake animation for invalid entry
                Animation animation = AnimationUtils.loadAnimation(curActivity.getBaseContext(), R.anim.shake);
                curLL.startAnimation(animation);
            }
            else{
                String guess = (String) ((TextView)tileIDArr[curRow - 1][0]).getText() +
                        (String) ((TextView)tileIDArr[curRow - 1][1]).getText() +
                        (String) ((TextView)tileIDArr[curRow - 1][2]).getText() +
                        (String) ((TextView)tileIDArr[curRow - 1][3]).getText() +
                        (String) ((TextView)tileIDArr[curRow - 1][4]).getText();
                Set<String> dictionary = model.getDictionary();
                if (dictionary.contains(guess.toLowerCase())) {
                    model.makeGuess(curRow-1,guess);
                    // update board and determine endGame
                    updateBoard();
                    curRow++;
                    curCol = 1;
                    updateKeyboard();
                    // Display end of game prompt if game reaches max guess or correct guess
                    if(isOver){
                        player.updateTotalPlays();
                        player.updateCurStreak(didWin);
                        player.updateTotalWins(didWin);
                        String msg = "";
                        if(didWin) {
                            int guesses = curRow - 1;
                            player.updateGuessHistory(guesses);
                            if(guesses == 1)
                                msg = "SPECTACULAR!";
                            else if (guesses < 4)
                                msg = "GREAT JOB!";
                            else if (guesses == 4)
                                msg = "GOOD JOB!";
                            else if (guesses == 5)
                                msg = "YOU GOT IT!";
                            else
                                msg = "PHEW";

                            for (View e : tileIDArr[curRow - 2])
                                tileWinAnimator((TextView) e);
                        }
                        else
                            msg = model.getAnswer();
                        showFadingText(alertText, msg);
                    }
                } else {
                    showFadingText(alertText, "NOT IN WORD LIST");
                    // shake animation for invalid entry
                    Animation animation = AnimationUtils.loadAnimation(curActivity.getBaseContext(), R.anim.shake);
                    curLL.startAnimation(animation);
                }
            }
        }
    }

    /**
     * This method fades the alert text in and out
     * @param tv the alertText TextView
     * @param text the message to be displayed in tv
     */
    public void showFadingText(TextView tv, String text){
        tv.setText(text);
        Runnable endAction;
        tv.animate().alpha(1f).setDuration(1000).setStartDelay(0).withEndAction(
                endAction = new Runnable() {
                    public void run() {
                        tv.animate().alpha(0f).setDuration(1000).setStartDelay(1000);
                    }
                }
        );
    }

    /**
     *
     * This method updates the keyboard colors
     *
     */
    public void updateKeyboard(){
        // each letter's guess current status in the game
        INDEX_RESULT[] keyboardStatus = model.getGuessedCharacters();
        for (int j = 0; j < keyArr.length; j++ ){
            Button curBtn = ((Button)keyArr[j]);
            if(keyboardStatus[j] == INDEX_RESULT.INCORRECT ||
                    keyboardStatus[j] == INDEX_RESULT.CORRECT_WRONG_INDEX
                    || keyboardStatus[j] == INDEX_RESULT.CORRECT)
                curBtn.setTextColor(ContextCompat.getColor(curActivity.
                        getBaseContext(), R.color.white));
            if(keyboardStatus[j] == INDEX_RESULT.INCORRECT)
                curBtn.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(curActivity.
                                getBaseContext(), R.color.dark_grey)));
            else if(keyboardStatus[j] == INDEX_RESULT.CORRECT_WRONG_INDEX)
                curBtn.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(curActivity.
                                getBaseContext(), R.color.gold)));
            else if(keyboardStatus[j] == INDEX_RESULT.CORRECT)
                curBtn.setBackgroundTintList(ColorStateList.
                        valueOf(ContextCompat.getColor(curActivity.
                                getBaseContext(), R.color.green)));
        }
    }

    /**
     *
     * This method updates the game board tiles
     *
     */
    public void updateBoard(){
        Guess[] guessArr = model.getProgress();
        // if true, the board is getting updated based on previously-saved data
        boolean calledOnCreate = (curCol != 6);
        // number of rows to update
        int rowToUpdate = 1;
        if(calledOnCreate) {
            rowToUpdate = curRow - 1;
            didWin = guessArr[curRow - 2].getIsCorrect();
        }
        else
            didWin = guessArr[curRow-1].getIsCorrect();
        isOver = (didWin || (curRow > 5 && curCol == 6) || (curRow>6));
        for (; rowToUpdate > 0; rowToUpdate--) {
            int i = 0;
            // the row to update
            View toIterate[];
            if(calledOnCreate)
                toIterate = tileIDArr[curRow - 1 - (rowToUpdate)];
            else
                toIterate = tileIDArr[curRow - 1];
            for (View temp : toIterate) {
                TextView curTV = ((TextView) temp);
                // shrink animation for update
                Animation shrinkBtn = AnimationUtils.loadAnimation(curActivity.
                        getBaseContext(),R.anim.shrink);
                curTV.setAnimation(shrinkBtn);
                curTV.setTextColor(ContextCompat.getColor(curActivity.
                        getBaseContext(), R.color.white));
                INDEX_RESULT cur;
                if(calledOnCreate) {
                    cur = guessArr[curRow - 1 - (rowToUpdate)].getIndices()[i];
                    curTV.setText(Character.toString(guessArr[curRow - 1 -
                            (rowToUpdate)].getGuess().charAt(i)));
                }
                else
                    cur = guessArr[curRow - 1 ].getIndices()[i];
                if (cur == INDEX_RESULT.INCORRECT)
                    curTV.setBackgroundResource(R.drawable.tile_wrong);
                else if (cur == INDEX_RESULT.CORRECT_WRONG_INDEX)
                    curTV.setBackgroundResource(R.drawable.tile_almost);
                else if (cur == INDEX_RESULT.CORRECT)
                    curTV.setBackgroundResource(R.drawable.tile_right);
                i++;
                // expand animation for update
                Animation expandBtn = AnimationUtils.loadAnimation(curActivity.
                        getBaseContext(),R.anim.expand);
                curTV.setAnimation(expandBtn);
            }
        }
    }

    /**
     * The method for launching the leaderboard fragment
     * @param result the results to be displayed on the leaderboard
     */
    private void launchRankFragment(String result){
        LeaderBoardFragment rankFrag = new LeaderBoardFragment();
        rankFrag.setContainerActivity(this);
        Bundle args = new Bundle();
        args.putString(QUERY_RESULT, result);
        rankFrag.setArguments(args);
        rankFrag.show(getSupportFragmentManager(), "Rank Fragment");
    }

    /**
     *
     * This method animates the winning tiles by an up and a down motion
     * @param tv the tile to animate
     */
    private void tileWinAnimator(TextView tv){
        PropertyValuesHolder pX = PropertyValuesHolder.ofFloat("translationY",-50);
        PropertyValuesHolder pXReturn = PropertyValuesHolder.ofFloat("translationY",0);
        ObjectAnimator animatorX = ObjectAnimator.ofPropertyValuesHolder(tv, pX);
        animatorX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator animatorXReturn = ObjectAnimator.ofPropertyValuesHolder(tv, pXReturn);
                animatorXReturn.setDuration(1000);
                animatorXReturn.start();
            }
        });
        animatorX.setDuration(1000);
        animatorX.start();

    }
    /**
     * this method populates our 2D array of tiles
     * @return View[][] res is our 2D array of tiles
     */
    private View[][] tileIDArrayBuilder(){
        View[][] res =  new View[][]{{findViewById(R.id.tile_11), findViewById(R.id.tile_12),
                findViewById(R.id.tile_13), findViewById(R.id.tile_14),
                findViewById(R.id.tile_15)},
                {findViewById(R.id.tile_21), findViewById(R.id.tile_22),
                        findViewById(R.id.tile_23), findViewById(R.id.tile_24),
                        findViewById(R.id.tile_25)},
                {findViewById(R.id.tile_31), findViewById(R.id.tile_32),
                        findViewById(R.id.tile_33), findViewById(R.id.tile_34),
                        findViewById(R.id.tile_35)},
                {findViewById(R.id.tile_41), findViewById(R.id.tile_42),
                        findViewById(R.id.tile_43), findViewById(R.id.tile_44),
                        findViewById(R.id.tile_45)},
                {findViewById(R.id.tile_51), findViewById(R.id.tile_52),
                        findViewById(R.id.tile_53), findViewById(R.id.tile_54),
                        findViewById(R.id.tile_55)},
                {findViewById(R.id.tile_61), findViewById(R.id.tile_62),
                        findViewById(R.id.tile_63), findViewById(R.id.tile_64),
                        findViewById(R.id.tile_65)}};
        return res;

    }

    /**
     * this method populates our  array of keys
     * @return View[] res is our  array of keys
     */
    private View[] keyArrBuilder(){
        View[] res = new View[]{findViewById(R.id.A), findViewById(R.id.B), findViewById(R.id.C),
                findViewById(R.id.D), findViewById(R.id.E), findViewById(R.id.F),
                findViewById(R.id.G), findViewById(R.id.H), findViewById(R.id.I),
                findViewById(R.id.J), findViewById(R.id.K), findViewById(R.id.L),
                findViewById(R.id.M), findViewById(R.id.N), findViewById(R.id.O),
                findViewById(R.id.P), findViewById(R.id.Q), findViewById(R.id.Rr),
                findViewById(R.id.S), findViewById(R.id.T), findViewById(R.id.U),
                findViewById(R.id.V), findViewById(R.id.W), findViewById(R.id.X),
                findViewById(R.id.Y), findViewById(R.id.Z)};
        return res;
    }

    /**
     * This method handles the click on our Help ImageView
     * @param view the view getting clicked on
     */
    public void onHelpClick(View view) {
        HelpFragment helpFragment = new HelpFragment();
        helpFragment.setContainerActivity(curActivity);
        helpFragment.show(getSupportFragmentManager(),"Help Fragment");
    }

    /**
     * This method handles the click on our Stats ImageView
     * @param view the view getting clicked on
     */
    public void onStatsClick(View view) {
        StatsFragment statFragment = new StatsFragment();
        statFragment.setContainerActivity(curActivity);
        Bundle args = new Bundle();
        args.putBoolean(REQUEST_IS_OVER,isOver);
        args.putBoolean(REQUEST_DID_WIN,didWin);
        statFragment.setArguments(args);
        statFragment.show(getSupportFragmentManager(),"Stats Fragment");
    }

    /**
     * This method handles the click on our Settings ImageView
     * @param view the view getting clicked on
     */
    public void onSettingsClick(View view) {
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setContainerActivity(curActivity);
        settingsFragment.show(getSupportFragmentManager(),"Settings Fragment");
    }

    /**
     * This is the async task that handles communications with the server
     * doInBackground does the actual communication.
     * onPostExecute processes and sends the information to the appropriate place in our game.
     * */
    private class OnlineCommunication extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {
            String data = "";

            String WorkType = params[0];
            String url = params[1];
            String connectionType = params[2];

            HttpURLConnection httpURLConnection = null;

            try {
                httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                httpURLConnection.setRequestMethod(connectionType);

                if (connectionType.equals(getString(R.string.post_type))) {
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setConnectTimeout(15000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                   httpURLConnection.addRequestProperty("Accept", "application/json");
                   httpURLConnection.addRequestProperty("Content-Type", "application/json");
                   OutputStream os = httpURLConnection.getOutputStream();
                   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                   String postdata = "{\"PostData\":" + params[3] + "}";
                   Log.e("TAG", postdata);
                   writer.write(postdata);
                   writer.flush();
                   writer.close();
                   os.close();

                   httpURLConnection.connect();
                }
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return new String[]{params[0], data};
        }

        /**
         * This method processes and sends the information to the appropriate place in our game.
         * @param result
         */
        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (result[0].equals(getString(R.string.saved_user_id_key))){
                setUserId(result[1]);
            } else if (result[0].equals(getString(R.string.get_highscores))){
                launchRankFragment(result[1]);
            }
            // this is expecting a response code to be sent from your server upon receiving the POST data
            Log.e("TAG", result[1]);
        }
    }
}