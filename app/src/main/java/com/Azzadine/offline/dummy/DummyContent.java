package com.Azzadine.offline.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();


    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();


    static {
        // Add some sample items.

        addItem(createDummyItem(1, "آه يا زمان الغدار", "mp3", "00:01"));
        addItem(createDummyItem(2, "الحب الي جفا عليا", "mp3", "00:01"));
        addItem(createDummyItem(3, "انا خيك انا خيك", "mp3", "00:01"));
        addItem(createDummyItem(4, "توحشتك يلميمة", "mp3", "00:01"));
        addItem(createDummyItem(5, "جاتني على الرابعة", "mp3", "00:01"));
        addItem(createDummyItem(6, "دور الطاسة", "mp3", "00:01"));
        addItem(createDummyItem(7, "شوف الحقرة شوف", "mp3", "00:01"));
        addItem(createDummyItem(8, "ضاق عليا الحال لميمة", "mp3", "00:01"));
        addItem(createDummyItem(9, "طريق حيدرة", "mp3", "00:00"));
        addItem(createDummyItem(10, "عندي لي نبغيها", "mp3", "00:01"));
 



    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int number, String songNmae, String extension, String timeMusic) {
        return new DummyItem("" + number, songNmae, extension, timeMusic);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String content;
        public final String details;
        public final String timeMusic;
        public DummyItem(String id, String content, String details, String timeMusic) {
            this.id = id;
            this.content = content;
            this.details = details;
            this.timeMusic = timeMusic;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
