package com.example.myapplication.imageparsing;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PollAfter {

    @SerializedName("pollAfter")
    @Expose
    private Integer pollAfter;

    @SerializedName("description")
    @Expose
    private String description;

    public Integer getPollAfter() {
        return pollAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setPollAfter(Integer pollAfter) {
        this.pollAfter = pollAfter;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PollAfter{" +
                "pollAfter=" + pollAfter +
                ", description='" + description + '\'' +
                '}';
    }
}
