// Written by the Visualizer from TeamCode/autos/spots.json, together with it, whenever the team's
// spots are saved. Never edit it by hand: move the spot in the Visualizer and save.
// TeamSpotsTest fails while this file and spots.json differ.
package org.firstinspires.ftc.teamcode.localization.generated;

import org.firstinspires.ftc.teamcode.localization.TeamSpot;

/** The team's spots, drawn for RED: x, y in inches, heading in degrees (NaN: reached at any heading). */
public final class TeamSpots {

    private TeamSpots() {
    }

    public static final TeamSpot GARDEN_STAGING = new TeamSpot("GARDEN_STAGING", 11.5, 22.1, 270.0);
    public static final TeamSpot LEFT_AUTO_HIVE = new TeamSpot("LEFT_AUTO_HIVE", 61.9, 120.4, 268.1);
    public static final TeamSpot LEFT_AUTO_OFFSET = new TeamSpot("LEFT_AUTO_OFFSET", 40.7, 121.1, 298.7);
    public static final TeamSpot LEFT_PARK = new TeamSpot("LEFT_PARK", 14.5, 117.4, 0.0);
    public static final TeamSpot LEFT_START = new TeamSpot("LEFT_START", 62.3, 133.8, 270.0);
    public static final TeamSpot PARTNER_STAGED_POLLEN = new TeamSpot("PARTNER_STAGED_POLLEN", 25.6, 130.8, 95.0);
    public static final TeamSpot RIGHT_AUTO_FIX_TIP = new TeamSpot("RIGHT_AUTO_FIX_TIP", 55.3, 25.7, 90.0);
    public static final TeamSpot RIGHT_AUTO_HIVE = new TeamSpot("RIGHT_AUTO_HIVE", 59.2, 20.9, 96.1);
    public static final TeamSpot RIGHT_AUTO_OFFSET = new TeamSpot("RIGHT_AUTO_OFFSET", 32.1, 16.4, 61.0);
    public static final TeamSpot RIGHT_PARK = new TeamSpot("RIGHT_PARK", 14.5, 98.7, 0.0);
    public static final TeamSpot RIGHT_START = new TeamSpot("RIGHT_START", 55.6, 7.7, 90.0);
    public static final TeamSpot SCORING_FLOWER_STAGING = new TeamSpot("SCORING_FLOWER_STAGING", 47.2, 119.3, 90.0);
    public static final TeamSpot WALL_FLOWER_STAGING = new TeamSpot("WALL_FLOWER_STAGING", 23.1, 47.2, 180.0);

    /** Every spot, in spots.json's order. */
    public static TeamSpot[] all() {
        return new TeamSpot[] {GARDEN_STAGING, LEFT_AUTO_HIVE, LEFT_AUTO_OFFSET, LEFT_PARK, LEFT_START, PARTNER_STAGED_POLLEN, RIGHT_AUTO_FIX_TIP, RIGHT_AUTO_HIVE, RIGHT_AUTO_OFFSET, RIGHT_PARK, RIGHT_START, SCORING_FLOWER_STAGING, WALL_FLOWER_STAGING};
    }
}
