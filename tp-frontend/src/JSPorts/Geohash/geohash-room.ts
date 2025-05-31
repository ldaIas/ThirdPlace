import Geohash from 'ngeohash';

import type { App } from "../types/apptypes"

export function setupRoomPorts(app: App): void {
    if (!app.ports || !app.ports.requestRoomId || !app.ports.receiveRoomId) {
        console.error("Missing ports for room ID resolution");
        return;
    }

    app.ports.requestRoomId.subscribe((): void => {
        console.log("Requesting room ID");
        if (!navigator.geolocation) {
            console.log("Geolocation not supported");
            app.ports.receiveRoomId.send("unknown");
            return;
        }

        navigator.geolocation.getCurrentPosition((pos): void => {
            console.log("pos:", pos);
            const coords = pos.coords;
            const lat = coords.latitude;
            const lon = coords.longitude;
            console.log("Geolocation:", lat, lon);
            const geohash: string = Geohash.encode(lat, lon, 3);
            console.log("Geohash:", geohash);
            app.ports.receiveRoomId.send(geohash);
        }, 
            (err): void => {
                console.error("Geolocation error:", err);
                app.ports.receiveRoomId.send("unknown");
            });
    });
}
