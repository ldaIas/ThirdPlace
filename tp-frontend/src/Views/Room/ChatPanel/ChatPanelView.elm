module Views.Room.ChatPanel.ChatPanelView exposing (..)

import Html exposing (Html, button, div, h2, text, textarea)
import Html.Attributes exposing (class, disabled, placeholder, value)
import Html.Events exposing (onClick, onInput)
import ThirdPlaceModel
import Views.Room.ChatPanel.ChatPanelStyles as ChatPanelStyles
import Views.Room.RoomModel as RoomModel exposing (Msg(..), UsrChatMsg(..))
import Views.Room.RoomStyles as RoomStyles
import Websockets.Command exposing (Command(..))
import ThirdPlaceModel exposing (Msg(..))
import Json.Decode as Decode
import Html.Events exposing (preventDefaultOn)



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
                            , value selectedConvoModel.draftMessage
                            , onInput (ThirdPlaceModel.RoomMsg << ChatPanelMsg << DraftMessage)
                            , onEnter (ChatPanelMsg SendMessage)
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

{-
    When the Enter key is pressed, send the message passed in
-}
onEnter : RoomModel.Msg -> Html.Attribute ThirdPlaceModel.Msg
onEnter msg =
     let
        convertedTPMsg : ThirdPlaceModel.Msg
        convertedTPMsg =
            RoomMsg msg

        enterDecoder =
            Decode.field "key" Decode.string
                |> Decode.map
                    (\key ->
                        if key == "Enter" then
                            ( convertedTPMsg, True )
                        else
                            ( NoOp, False )
                    )
    in
    preventDefaultOn "keydown" enterDecoder
