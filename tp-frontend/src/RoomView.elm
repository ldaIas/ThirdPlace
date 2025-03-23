module RoomView exposing (view)

import Html exposing (Html, text, div)
import ThirdPlaceModel exposing (Model, Msg(..))

view : Model -> Html Msg
view _ =
    div [] [ text "Room View" ]