port module JSPorts.Geohash.GeohashPorts exposing (requestRoomId, receiveRoomId)


port requestRoomId : (() -> Cmd msg)
port receiveRoomId : (String -> msg) -> Sub msg