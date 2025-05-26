import Geohash from 'ngeohash'

export function setupRoomPorts(app) {
    if (!app.ports || !app.ports.requestRoomId || !app.ports.receiveRoomId) {
        console.error("Missing ports for room ID resolution")
        return;
    }

    app.ports.requestRoomId.subscribe(() => {
        console.log("Requesting room ID")
        if (!navigator.geolocation) {
            console.log("Geolocation not supported")
            app.ports.receiveRoomId.send("unknown")
            return;
        }

        navigator.geolocation.getCurrentPosition((pos) => {
            console.log("pos:", pos)
            const coords = pos.coords
            const lat = coords.latitude
            const lon = coords.longitude
            console.log("Geolocation:", lat, lon)
            const geohash = Geohash.encode(lat, lon, 3);
            console.log("Geohash:", geohash)
            app.ports.receiveRoomId.send(geohash);
        },
            (err) => {
                console.error("Geolocation error:", err);
                app.ports.receiveRoomId.send("unknown")
            })
    })
}