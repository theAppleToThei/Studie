package com.techtalk4geeks.studie;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alex on 1/30/16.
 */
public class QuizletSet {

    String title;
    String description;
    String createdBy;
    String URL;
    String id;
    int termCount;
    ArrayList<String> terms = new ArrayList<String>();
    String termLanguage;
    ArrayList<String> definitions = new ArrayList<String>();
    String definitionLanguage;
    String creatorName;
    String apiLink;
    ArrayList<QuizletGroup> groups = new ArrayList<QuizletGroup>();

    public static final String S = "Studie";

    public QuizletSet(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        title = jsonOb.getString("title");
        description = jsonOb.getString("description");
        createdBy = jsonOb.getString("created_by");
        URL = jsonOb.getString("url");
        id = jsonOb.getString("id");
        try {
            termCount = Integer.parseInt(jsonOb.getString("term_count"));
        } catch (NumberFormatException nf) {
            Log.e("Studie", "NUMBER FORMAT EXCEPTION ON TERM COUNT!!!");
            termCount = -1;
        }
        termLanguage = jsonOb.getString("lang_terms");
        definitionLanguage = jsonOb.getString("lang_definitions");
        creatorName = jsonOb.getString("created_by");

        JSONArray arr = jsonOb.getJSONArray("terms");
        for (int i = 0; i < arr.length(); i++) {
            terms.add(i, arr.getJSONObject(i).getString("term"));
            definitions.add(i, arr.getJSONObject(i).getString("definition"));
        }

        Log.i("Studie", "Set Created\n--- " + title + " ---\n" + createdBy + "\n" + description);
    }

    public String getDebugSummary() {
        Log.i("Studie", "--- " + title + " ---\n" + createdBy + "\n" + description);
        return "--- " + title + " ---\n" + createdBy + "\n" + description;
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

    public String getDescription() {
        return description;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setAPILink(String apiLink) {
        this.apiLink = apiLink;
    }

    public String getAPILink() {
        return apiLink;
    }
}
