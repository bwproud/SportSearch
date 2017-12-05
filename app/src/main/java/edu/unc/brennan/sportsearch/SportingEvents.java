package edu.unc.brennan.sportsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unc.brennan.sportsearch.dummy.DummyContent;

/**
 * Created by Brennan on 11/26/17.
 */

public class SportingEvents {
    /**
     * An array of sample (dummy) items.
     */
    public static final List<Event> ITEMS = new ArrayList<>();
    public static String[] sports = {"soccer", "basketball", "tennis", "football"};
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Event> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Event item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Event createDummyItem(int position) {
        //String id, String sport, String date, String participants, String location, String name
        return new Event(String.valueOf(position), sports[position%4], "NOW",String.valueOf(position), "Lenoir", "Sample Name");
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("TEST\n");
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
