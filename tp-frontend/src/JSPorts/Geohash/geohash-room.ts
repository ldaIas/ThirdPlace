import Geohash from 'ngeohash';

import type { App } from "../../types/apptypes"

/**
 * Sets up ports for location-based room identification.
 * This module handles geolocation and converts coordinates to a geohash
 * which is used as a room identifier for local conversations.
 */
export function setupRoomPorts(app: App): void {

    // Subscribe to requests from Elm to get the room hash ID
    app.ports.requestRoomId.subscribe((): void => {
        console.log("Requesting room ID");
        
        // Check if geolocation is available in the browser
        if (!navigator.geolocation) {
            console.warn("Geolocation not supported");
            alert("You must allow location services to be able to connect to a room.")
            app.ports.receiveRoomId.send("unknown");
            return;
        }

        // Request the user's current position to determine their room hash
        navigator.geolocation.getCurrentPosition((pos): void => {
            console.log("pos:", pos);
            const coords = pos.coords;
            const lat = coords.latitude;
            const lon = coords.longitude;
            console.log("Geolocation:", lat, lon);
            
            // Convert coordinates to a geohash with precision 3
            // This creates location-based rooms that cover roughly 156km×156km areas
            // This will probably be fine tuned or "scaled" later
            const geohash: string = Geohash.encode(lat, lon, 3);
            console.log("Geohash:", geohash);
            
            // Send the geohash back to Elm as the room ID
            app.ports.receiveRoomId.send(geohash);
        },
            (err): void => {
                // Handle geolocation errors by sending "unknown" as the room ID
                console.error("Geolocation error:", err);
                app.ports.receiveRoomId.send("unknown");
            });
    });
}
