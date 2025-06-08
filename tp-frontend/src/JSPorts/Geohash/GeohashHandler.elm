module JSPorts.Geohash.GeohashHandler exposing (..)

import JSPorts.Geohash.GeohashPorts exposing (receiveRoomId)
import JSPorts.RoomPubsub.RoomPubsubPorts as RoomPubsubPorts


type alias Model =
    { roomId : String -- "unknown" is used for unresolved room ids
    }


type Msg
    = GotRoomId String -- When we recieve the geohashed id from js


init : Model 
init =
    Model "unknown" 


update : Model -> Msg -> ( Model, Cmd Msg )
update model msg =
    case msg of
        GotRoomId roomId ->
            -- After getting the room hash, try to join the room pubsub node
            ( { model | roomId = roomId }, Debug.log "reaching out to join room via js" RoomPubsubPorts.joinRoom roomId )



{-
   The subscriptions here does not need a model; we are merely requesting a value from JS
-}


subscriptions : Model -> Sub Msg
subscriptions _ =
    receiveRoomId GotRoomId
