module Views.Room.ChatPanel.ChatPanelView exposing (..)

import Html exposing (Html, button, div, h2, text, textarea)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onInput)
import Views.Room.ChatPanel.ChatPanelStyles as ChatPanelStyles
import Views.Room.RoomModel as RoomModel
import Views.Room.RoomStyles as RoomStyles

{-
    Display the chat panel when a conversation is selected.
    If no convo is selected then displays the closed panel
-}
view : RoomModel.Model -> Html msg
view model =
    div [ class RoomStyles.container, class ChatPanelStyles.chatPanel ]
        -- If there is a convo selected display the chat panel otherwise display nothing
        (case model.selectedConvo of
            Nothing ->
                []

            Just selectedConvoModel ->
                [ div [ class "chat-container" ]
                    [ div [ class "chat-header" ]
                        [ div [ class "chat-topic" ] [ text selectedConvoModel.intro ]
                        , div [ class "chat-author" ] [ text selectedConvoModel.author ]
                        ]
                    , div [ class "chat-messages" ]
                        (List.map
                            (\msg ->
                                let
                                    selfOrOtherMsgClass : String
                                    selfOrOtherMsgClass =
                                        if msg.sender == "current_user" then
                                            "message-from-self"

                                        else
                                            "message-from-other"
                                in
                                div [ class selfOrOtherMsgClass ]
                                    [ div [ class "sender" ] [ text msg.sender ]
                                    , div [ class "content" ] [ text msg.content ]
                                    ]
                            )
                            selectedConvoModel.messages
                        )
                    , div [ class "chat-input" ]
                        [ textarea
                            [ placeholder "Type your message..."
                            ]
                            []
                        , button [ class "send-button" ] [ text "Send" ]
                        ]
                    ]
                ]
        )
