package edu.calvin.cs262.lab06;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by jjh35 on 10/17/2016.
 * This class provides a player class for MainActivity
 */
public class Players {
    private String name, email;
    private int id;

    public Players( int id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public int getID()
    {
        return id;
    }
    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

}
