module Views.Room.ChatPanel.ChatPanelView exposing (..)

import Html exposing (Html, div, h2, text)
import Html.Attributes exposing (class)
import Views.Room.RoomModel as RoomModel
import Views.Room.RoomStyles as RoomStyles
import Views.Room.ChatPanel.ChatPanelStyles as ChatPanelStyles


view : RoomModel.Model -> Html msg
view _ =
    div [ class RoomStyles.container, class ChatPanelStyles.chatPanel ]
        [ h2 [] [ text "Selected Conversation" ] ]
