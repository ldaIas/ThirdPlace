module Utils.RoomUtils exposing (..)

import Json.Decode as Decode
import Html
import ThirdPlaceModel
import ThirdPlaceModel exposing (Msg(..))
import Html.Events exposing (preventDefaultOn)

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
