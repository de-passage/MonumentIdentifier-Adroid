package com.example.watchapplication;

import com.google.gson.annotations.SerializedName;

public class Monument {
    String country;
    String lang;
    String project;
    String id;
    String adm0;
    String adm1;
    String adm2;
    String adm3;
    String adm4;
    String name;
    String address;
    String municipality;
    Double lat;
    Double lon;
    String image;
    String commonscat;
    String source;
    @SerializedName("monument_article")
    String monumentArticle;
    @SerializedName("wd_item")
    String wdItem;
    @SerializedName("registrant_url")
    String registrantUrl;
    String changed;
    Double dist;
}

