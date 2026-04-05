package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Run this Activity ONCE to seed demo places into Firebase.
 * After seeding, remove the <activity> entry from AndroidManifest.xml
 * and delete or comment out this file.
 *
 * To trigger it temporarily, add to AndroidManifest.xml inside <application>:
 *   <activity android:name=".SeedDataActivity" android:exported="true"/>
 * Then launch via: adb shell am start -n com.example.finalproject/.SeedDataActivity
 */
public class SeedDataActivity extends AppCompatActivity {

    private static final String TAG = "SeedData";
    private TextView tvStatus;
    private int successCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvStatus = new TextView(this);
        tvStatus.setPadding(48, 48, 48, 16);
        tvStatus.setTextSize(14);
        tvStatus.setText("Press button to seed demo places into Firebase...");

        Button btnSeed = new Button(this);
        btnSeed.setText("Seed Demo Data");
        btnSeed.setOnClickListener(v -> seedData());

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(tvStatus);
        layout.addView(btnSeed);
        setContentView(layout);
    }

    private void seedData() {
        DatabaseReference placesRef = FirebaseDatabase.getInstance().getReference("places");
        List<DemoPlace> places = buildDemoPlaces();
        successCount = 0;
        tvStatus.setText("Seeding " + places.size() + " places...");

        for (DemoPlace dp : places) {
            String id = placesRef.push().getKey();
            if (id == null) continue;

            com.example.finalproject.models.Place place =
                    new com.example.finalproject.models.Place(
                            dp.title, dp.description, dp.imageUrl,
                            dp.timestamp, "demo_user_001"
                    );
            place.setId(id);
            place.setLat(dp.lat);
            place.setLng(dp.lng);
            place.setLikesCount(dp.likes);

            placesRef.child(id).setValue(place).addOnSuccessListener(v -> {
                successCount++;
                tvStatus.setText("Seeded " + successCount + " / " + places.size());
                if (successCount == places.size()) {
                    tvStatus.setText("Done! All " + successCount + " places seeded.");
                    Toast.makeText(this, "Seeding complete!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                    Log.e(TAG, "Failed to seed: " + dp.title + " - " + e.getMessage()));
        }
    }

    private List<DemoPlace> buildDemoPlaces() {
        List<DemoPlace> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        long hour = 3600_000L;

        // Ireland / Dublin area
        list.add(new DemoPlace(
                "Cliffs of Moher",
                "Breathtaking 214-metre sea cliffs on the west coast of Ireland. Best visited at sunrise before the tour buses arrive. The walk along the cliff edge towards O'Brien's Tower is unforgettable.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Cliffs_of_Moher_2012.jpg/1280px-Cliffs_of_Moher_2012.jpg",
                52.9720, -9.4263, now - 2 * hour, 142
        ));

        list.add(new DemoPlace(
                "Trinity College Dublin",
                "Home to the Book of Kells, one of the world's most famous illuminated manuscripts. The Long Room library alone is worth the visit. Book tickets online to avoid the queue.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/TCD_front_square.jpg/1280px-TCD_front_square.jpg",
                53.3438, -6.2546, now - 5 * hour, 89
        ));

        list.add(new DemoPlace(
                "Killarney National Park",
                "Ireland's oldest national park covering over 100 sq km of lakes, woodland and mountain. Rent a bike in town and cycle the Gap of Dunloe for stunning views. Watch out for the resident red deer.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/Killarney_National_Park.jpg/1280px-Killarney_National_Park.jpg",
                52.0543, -9.5044, now - 10 * hour, 203
        ));

        list.add(new DemoPlace(
                "Giant's Causeway",
                "Around 40,000 interlocking basalt columns formed by ancient volcanic activity. UNESCO World Heritage Site in Northern Ireland. The coastal walk extends for miles in both directions.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Causeway-code_poet-4.jpg/1280px-Causeway-code_poet-4.jpg",
                55.2408, -6.5116, now - 24 * hour, 317
        ));

        list.add(new DemoPlace(
                "Rock of Cashel",
                "A spectacular medieval fortress rising from the Tipperary plain. The round tower, Romanesque chapel and Gothic cathedral are exceptionally preserved. Visit at golden hour for the best photos.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Rock_of_Cashel%2C_Tipperary.jpg/1280px-Rock_of_Cashel%2C_Tipperary.jpg",
                52.5205, -7.8894, now - 48 * hour, 76
        ));

        // Europe
        list.add(new DemoPlace(
                "Eiffel Tower, Paris",
                "An iconic iron lattice tower standing 330 metres tall on the Champ de Mars. Take the stairs to the second floor for a workout and great views, then the lift to the top. Book summit tickets weeks in advance.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/85/Smiley.svg/200px-Smiley.svg.png",
                48.8584, 2.2945, now - 72 * hour, 521
        ));

        list.add(new DemoPlace(
                "Colosseum, Rome",
                "The largest amphitheatre ever built, capable of holding 80,000 spectators. The underground hypogeum where gladiators and animals waited is open on guided tours. Arrive at opening time to beat the crowds.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/de/Colosseo_2020.jpg/1280px-Colosseo_2020.jpg",
                41.8902, 12.4922, now - 96 * hour, 488
        ));

        list.add(new DemoPlace(
                "Santorini Caldera",
                "The clifftop village of Oia offers one of the world's most photographed sunsets. Stay in a cave hotel carved into the volcanic rock. The black sand beach at Perissa is quieter than Kamari.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Santorini_-_Oia_01.jpg/1280px-Santorini_-_Oia_01.jpg",
                36.4618, 25.3753, now - 120 * hour, 394
        ));

        list.add(new DemoPlace(
                "Sagrada Familia, Barcelona",
                "Gaudi's unfinished masterpiece and Spain's most visited monument. The interior forest of columns and stained glass is unlike anything else on earth. Book timed entry tickets months ahead.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/73/2019_La_Sagrada_Fam%C3%ADlia_-_Barcelona%2C_Spain.jpg/1280px-2019_La_Sagrada_Fam%C3%ADlia_-_Barcelona%2C_Spain.jpg",
                41.4036, 2.1744, now - 144 * hour, 276
        ));

        list.add(new DemoPlace(
                "Neuschwanstein Castle",
                "The fairytale castle that inspired Disney's Sleeping Beauty castle, perched in the Bavarian Alps. Hike up to Marienbrucke bridge for the classic view. The interior tour is only 35 minutes but worth it.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Schloss_Neuschwanstein_2013.jpg/1280px-Schloss_Neuschwanstein_2013.jpg",
                47.5576, 10.7498, now - 168 * hour, 341
        ));

        // Americas
        list.add(new DemoPlace(
                "Machu Picchu, Peru",
                "A 15th century Inca citadel set high in the Andes at 2,430 metres. Take the early bus from Aguas Calientes to arrive before the clouds lift. The Sun Gate hike adds two hours but the panorama is worth every step.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/eb/Machu_Picchu%2C_Peru.jpg/1280px-Machu_Picchu%2C_Peru.jpg",
                -13.1631, -72.5450, now - 200 * hour, 612
        ));

        list.add(new DemoPlace(
                "Grand Canyon, Arizona",
                "277 miles of canyon carved by the Colorado River over 5 to 6 million years. The South Rim is open year round. Hike Bright Angel Trail early morning before the heat sets in and always carry more water than you think you need.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/aa/Dawn_on_the_S_rim_of_the_Grand_Canyon_%288645178272%29.jpg/1280px-Dawn_on_the_S_rim_of_the_Grand_Canyon_%288645178272%29.jpg",
                36.1069, -112.1129, now - 250 * hour, 445
        ));

        // Asia
        list.add(new DemoPlace(
                "Ha Long Bay, Vietnam",
                "Around 1,600 limestone islands and islets rising from emerald waters in the Gulf of Tonkin. A two-night cruise is far better than a day trip. Kayak into the sea caves at low tide.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/forty/Ha_Long_Bay.jpg/1280px-Ha_Long_Bay.jpg",
                20.9101, 107.1839, now - 300 * hour, 287
        ));

        list.add(new DemoPlace(
                "Fushimi Inari, Kyoto",
                "Thousands of vermillion torii gates winding up a mountain behind the Inari shrine. The full trail to the summit takes about 2 hours each way. Go early morning or evening to avoid the crowds at the base.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Fushimi_Inari_Taisha_torii_gates_2016-02-09.jpg/1280px-Fushimi_Inari_Taisha_torii_gates_2016-02-09.jpg",
                34.9671, 135.7727, now - 350 * hour, 398
        ));

        return list;
    }

    private static class DemoPlace {
        String title, description, imageUrl;
        double lat, lng;
        long timestamp;
        int likes;

        DemoPlace(String title, String description, String imageUrl,
                  double lat, double lng, long timestamp, int likes) {
            this.title       = title;
            this.description = description;
            this.imageUrl    = imageUrl;
            this.lat         = lat;
            this.lng         = lng;
            this.timestamp   = timestamp;
            this.likes       = likes;
        }
    }
}