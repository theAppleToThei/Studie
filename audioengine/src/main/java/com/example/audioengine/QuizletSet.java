package com.example.audioengine;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alex on 1/30/16.
 */
public class QuizletSet {

    String title;
    String URL;
    int termCount;
    ArrayList<String> terms = new ArrayList<String>();
    ArrayList<String> definitions = new ArrayList<String>();
    String apiLink;

    public static final String S = "Studie";

    public QuizletSet(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        title = jsonOb.getString("title");
        URL = "https://quizlet.com" + jsonOb.getString("url");
        try {
            termCount = Integer.parseInt(jsonOb.getString("term_count"));
            Log.i(S, "termCount = " + termCount);
        } catch (NumberFormatException nf) {
            Log.e(S, "NUMBER FORMAT EXCEPTION ON TERM COUNT!!!");
            termCount = -1;
        }

        JSONArray arr = jsonOb.getJSONArray("terms");
        for (int i = 0; i < arr.length(); i++) {
            terms.add(i, arr.getJSONObject(i).getString("term"));
            definitions.add(i, arr.getJSONObject(i).getString("definition"));
        }
    }

    public void printTermsAndDefinitions() {
        Log.i("Studie", "Printing terms and definitions for " + title);
        for (int i = 0; i < terms.size(); i++) {
            Log.i("Studie", terms.get(i) + " --- " + definitions.get(i));
        }
    }

    public String getTitle() {
        return title;
    }

    public void setAPILink(String apiLink) {
        this.apiLink = apiLink;
    }

    public String getAPILink() {
        return apiLink;
    }

    public String getURL() {
        return URL;
    }

    public int getTermCount() {
        return termCount;
    }

    public ArrayList getTerms() {
        return terms;
    }

    public ArrayList getDefinitions() {
        return definitions;
    }
}
