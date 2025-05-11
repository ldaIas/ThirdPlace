module Views.Room.ChatPanel.ChatPanelView exposing (..)

import Html exposing (Html, button, div, h2, text, textarea)
import Html.Attributes exposing (class, classList, disabled, placeholder, value)
import Html.Events exposing (onClick, onInput, preventDefaultOn)
import Json.Decode as Decode
import ThirdPlaceModel exposing (Msg(..))
import Utils.RoomUtils as RoomUtils
import Views.Room.ChatPanel.ChatPanelStyles as ChatPanelStyles
import Views.Room.RoomModel as RoomModel exposing (Msg(..), UsrChatMsg(..))
import Views.Room.RoomStyles as RoomStyles
import Websockets.Command exposing (Command(..))



{-
   Display the chat panel when a conversation is selected.
   If no convo is selected then displays the closed panel
-}


view : RoomModel.Model -> Html ThirdPlaceModel.Msg
view model =
    div [ class RoomStyles.container, class ChatPanelStyles.chatPanel ]
        -- If there is a convo selected display the chat panel otherwise display nothing
        (case model.selectedConvo of
            selectedConvoModel ->
                [ div [ classList [ ( "chat-container", True ) ] ]
                    [ div [ class "chat-header" ]
                        -- The title of the convo and author
                        [ if model.panelExpansion then
                            div [] [ button [ onClick (RoomMsg (TogglePanels False)) ] [ text "<" ] ]

                          else
                            div [] [ button [ onClick (RoomMsg (TogglePanels True)) ] [ text ">" ] ]
                        , div [ class "chat-topic" ] [ text selectedConvoModel.intro ]
                        , div [ class "chat-author" ] [ text selectedConvoModel.author ]
                        ]
                    , div [ class "chat-messages" ]
                        -- The body of chat messages
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
                        -- The input block for sending a message
                        [ textarea
                            [ placeholder "Type your message..."
                            , value selectedConvoModel.draftMessage
                            , onInput (ThirdPlaceModel.RoomMsg << ChatPanelMsg << DraftMessage)
                            , RoomUtils.setupOnEnter (ThirdPlaceModel.RoomMsg (ChatPanelMsg SendMessage))
                            ]
                            []
                        , button
                            ([ class "send-button", onClick (ThirdPlaceModel.RoomMsg (ChatPanelMsg SendMessage)) ]
                                -- Disable the send button if in a system message (author is ThirdPlace)
                                ++ (if selectedConvoModel.author == "ThirdPlace" then
                                        [ disabled True ]

                                    else
                                        []
                                   )
                            )
                            [ text "Send" ]
                        ]
                    ]
                ]
        )
