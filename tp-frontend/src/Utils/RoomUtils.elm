module Utils.RoomUtils exposing (..)

import Json.Decode as Decode
import Json.Encode as Encode
import JSPorts.Identity.IdentityPorts
import Html
import ThirdPlaceModel
import ThirdPlaceModel exposing (Msg(..))
import Html.Events exposing (preventDefaultOn)


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

{-
    When the Enter key is pressed, send the message passed in
-}
setupOnEnter : ThirdPlaceModel.Msg -> Html.Attribute ThirdPlaceModel.Msg
setupOnEnter msg =
     let
        enterDecoder =
            Decode.field "key" Decode.string
                |> Decode.map
                    (\key ->
                        if key == "Enter" then
                            ( msg, True )
                        else
                            ( NoOp, False )
                    )
    in
    preventDefaultOn "keydown" enterDecoder
