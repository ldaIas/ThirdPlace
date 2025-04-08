module Views.Room.ChatPanel.ChatPanelView exposing (..)

import Html exposing (Html, button, div, h2, text, textarea)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onInput)
import Views.Room.ChatPanel.ChatPanelStyles as ChatPanelStyles
import Views.Room.RoomModel as RoomModel
import Views.Room.RoomStyles as RoomStyles


view : RoomModel.Model -> Html msg
view _ =
    div [ class RoomStyles.container, class ChatPanelStyles.chatPanel ]
        [ div [ class "chat-container" ]
            [ div [ class "chat-header" ]
                [ div [ class "chat-topic" ] [ text "Topic: Future of Decentralized Social" ]
                , div [ class "chat-author" ] [ text "Started by: codingcat" ]
                ]
            , div [ class "chat-messages" ]
                [ div [ class "message from-other" ]
                    [ div [ class "sender" ] [ text "user42" ]
                    , div [ class "content" ] [ text "I really think peer-to-peer is the way forward." ]
                    ]
                , div [ class "message from-self" ]
                    [ div [ class "sender" ] [ text "me" ]
                    , div [ class "content" ] [ text "Exactly. LibP2P has been solid so far." ]
                    ]
                ]
            , div [ class "chat-input" ]
                [ textarea
                    [ placeholder "Type your message..."
                    ]
                    []
                , button [] [ text "Send" ]
                ]
            ]
        ]
