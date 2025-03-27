module Utils.RoomUtils exposing (..)

import Json.Decode as Decode
import Json.Encode as Encode
import JSPorts.Identity.IdentityPorts


type alias ChatMessage =
    { username : String
    , message : String
    , conversationId : String
    }


encodeMessage : ChatMessage -> Encode.Value
encodeMessage message =
    Encode.object
        [ ( "username", Encode.string message.username )
        , ( "message", Encode.string message.message )
        , ( "conversationId", Encode.string message.conversationId )
        ]


decodeMessage : String -> Result String ChatMessage
decodeMessage val =
    Decode.decodeString
        (Decode.map3 ChatMessage
            (Decode.field "username" Decode.string)
            (Decode.field "message" Decode.string)
            (Decode.field "conversationId" Decode.string)
        )
        val
        |> Result.mapError Decode.errorToString


sendMessage : ChatMessage -> Cmd msg
sendMessage message =
    JSPorts.Identity.IdentityPorts.socket.send "chat" (encodeMessage message)
