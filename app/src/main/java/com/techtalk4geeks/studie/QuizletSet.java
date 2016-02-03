package com.techtalk4geeks.studie;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by alex on 1/30/16.
 */
public class QuizletSet {

    String title;
    String description;
    String createdBy;
    String URL;
    int termCount;
    ArrayList<String> terms;
    String termLanguage;
    ArrayList<String> definitions;
    String definitionLanguage;
    String creatorName;
    String apiLink;

    public static final String S = "Studie";

    public QuizletSet(String jsonString) throws Exception {
        JSONObject jsonOb;
        jsonOb = new JSONObject(jsonString);

        title = jsonOb.getString("title");
        description = jsonOb.getString("description");
        createdBy = jsonOb.getString("created_by");
        URL = jsonOb.getString("url");
        try {
            termCount = Integer.parseInt(jsonOb.getString("term_count"));
        } catch (NumberFormatException nf) {
            Log.e("Studie", "NUMBER FORMAT EXCEPTION ON TERM COUNT!!!");
            termCount = -1;
        }
        termLanguage = jsonOb.getString("lang_terms");
        definitionLanguage = jsonOb.getString("lang_definitions");
        creatorName = jsonOb.getString("created_by");
        Log.i("Studie", "Set Created\n--- " + title + " ---\n" + createdBy + "\n" + description);
    }

    public String getDebugSummary() {
        Log.i("Studie", "--- " + title + " ---\n" + createdBy + "\n" + description);
        return "--- " + title + " ---\n" + createdBy + "\n" + description;
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
