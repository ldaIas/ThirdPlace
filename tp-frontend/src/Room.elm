module Room exposing (..)

import Browser
import Browser.Dom as Dom exposing (..)
import Browser.Navigation as Navigation
import Html exposing (Html, button, div, input, p, text)
import Html.Attributes exposing (class, placeholder, style, type_, value)
import Html.Events exposing (on, onClick, onInput, onSubmit)
import RoomStyles exposing (..)
import RoomUtils
import Task
import Time
import Url exposing (Url)
import Utils.Ports
import Websockets exposing (WebsocketMessage)
import Html.Attributes exposing (id)


type SocketStatus
    = Opening
    | Open
    | Closed


type alias Model =
    { selectedConversation : Maybe String
    , currentUrl : Url
    , connected : SocketStatus
    , pendingMessage : String
    , messages : List RoomUtils.ChatMessage
    , time : Time.Posix
    }


init_model : Url -> Model
init_model url =
    { selectedConversation = Nothing
    , currentUrl = url
    , connected = Closed
    , pendingMessage = ""
    , messages = []
    , time = Time.millisToPosix 0
    }


type Msg
    = SelectConversation String
    | DeselectConversation
    | UrlChanged Url
    | NoOp
    | GotMessage String
    | SubmitMessage String
    | SendMessage RoomUtils.ChatMessage
    | SocketOpened
    | SocketMessage WebsocketMessage
    | SocketClosed
    | GotTime Time.Posix
    | GotTimeMs Int
    | AttemptReconnect


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ Time.every 1000 GotTime
        , Utils.Ports.socket.onEvent
            { onOpened = always SocketOpened
            , onClosed = always SocketClosed
            , onError = always NoOp
            , onMessage = SocketMessage
            , onDecodeError = always NoOp
            }
        ]


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SelectConversation conversation ->
            ( { model | selectedConversation = Just conversation }, Cmd.none )

        DeselectConversation ->
            ( { model | selectedConversation = Nothing }, Cmd.none )

        UrlChanged url ->
            ( { model | currentUrl = url }, Cmd.none )

        NoOp ->
            ( model, Cmd.none )

        GotTime time ->
            ( { model | time = time }, Cmd.none )

        GotTimeMs ms ->
            ( { model | time = Time.millisToPosix ms }, Cmd.none )

        GotMessage message ->
            ( { model | pendingMessage = message }, Cmd.none )

        SendMessage message ->
            ( { model | pendingMessage = "" }
            , RoomUtils.sendMessage message
            )

        SubmitMessage messageStr ->
            ( { model | pendingMessage = messageStr }
            , Cmd.none
            )

        SocketOpened ->
            ( { model | connected = Open }, Cmd.none )

        SocketClosed ->
            let
                disconnectMsg : RoomUtils.ChatMessage
                disconnectMsg =
                    { username = "server", message = "Disconnected from chat service.", conversationId = "" }
            in
            ( { model | connected = Closed, messages = model.messages ++ [ disconnectMsg ] }, Cmd.none )

        SocketMessage { data } ->
            case RoomUtils.decodeMessage data of
                Ok message ->
                    ( { model | messages = model.messages ++ [ message ] }
                    , jumpToBottom messagesContainer
                    )

                _ ->
                    ( model, Cmd.none )

        AttemptReconnect ->
            ( { model | connected = Opening }
            , Utils.Ports.socket.open "chat" "ws://localhost:8080/chat" []
            )


jumpToBottom : String -> Cmd Msg
jumpToBottom id =
    Dom.getViewportOf id
        |> Task.andThen (\info -> Dom.setViewportOf id 0 info.scene.height)
        |> Task.attempt (\_ -> NoOp)


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = mainView
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = \_ -> NoOp
        }


type alias Flags =
    ()


init : Flags -> Url -> Navigation.Key -> ( Model, Cmd Msg )
init _ url _ =
    ( init_model url
    , Cmd.batch
        [ Task.perform GotTime Time.now
        , Utils.Ports.socket.open "chat" "ws://localhost:8080/chat" []
        ]
    )


mainView : Model -> Browser.Document Msg
mainView model =
    { title = "Chat Room"
    , body =
        [ div [ class mainContainer, onClick DeselectConversation ]
            [ div [ class chatWindow ]
                [ div [ class circleBackground ]
                    [ div [ class leftConversation ]
                        [ conversationBubble "Hello" 1
                        , conversationBubble "How are you?" 0.6
                        , conversationBubble "I'm doing great!" 0.3
                        , conversationBubble "..." 0.1
                        ]
                    , div [ class rightConversation ]
                        [ conversationBubble model.pendingMessage 1
                        , conversationBubble "MEOW" 0.6
                        , conversationBubble "MEOW" 0.3
                        , conversationBubble "..." 0.1
                        ]
                    ]
                ]
            , case model.selectedConversation of
                Just conversation ->
                    div [ class conversationPanel ]
                        [ div [] [ text ("Full conversation: " ++ conversation) ] ]

                Nothing ->
                    div [] []
            , div [ class chatInputContainer ]
                [ div [ class messagesContainer, id messagesContainer ] [ viewConnectionStatus model.connected, viewMessages model.messages ]
                , div [ class inputContainer ]
                    [ Html.form [ class inputForm, onSubmit (SendMessage { username = "testuser", message = model.pendingMessage, conversationId = "ahhh" }) ]
                        [ Html.input
                            [ class chatInput
                            , type_ "text"
                            , placeholder "Say something..."
                            , value model.pendingMessage
                            , onInput SubmitMessage
                            ]
                            []
                        , button [ class sendButton, type_ "submit" ] [ text "💬" ]
                        ]
                    ]
                ]
            ]
        ]
    }


submitMessage : Model -> String -> Msg
submitMessage model msg =
    SendMessage
        { username = "testfront"
        , message = msg
        , conversationId = "general"
        }


viewMessages : List RoomUtils.ChatMessage -> Html msg
viewMessages messages =
    div []
        (List.map viewMessage messages)


viewMessage : RoomUtils.ChatMessage -> Html msg
viewMessage message =
    p [] [ text ("[" ++ message.username ++ "]: " ++ message.message) ]


conversationBubble : String -> Float -> Html Msg
conversationBubble message opacity =
    div [ class RoomStyles.conversationBubble, style "opacity" (String.fromFloat opacity) ]
        [ text message ]


getConnectionInfo : SocketStatus -> { statusClass : String, statusText : String, showRefresh : Bool }
getConnectionInfo status =
    case status of
        Open ->
            { statusClass = connected
            , statusText = "Connected"
            , showRefresh = False
            }

        Closed ->
            { statusClass = disconnected
            , statusText = "Disconnected"
            , showRefresh = True
            }

        Opening ->
            { statusClass = connecting
            , statusText = "Attempting to connect"
            , showRefresh = False
            }


viewConnectionStatus : SocketStatus -> Html Msg
viewConnectionStatus status =
    let
        info =
            getConnectionInfo status
    in
    div
        [ class connectionStatus
        , class info.statusClass
        ]
        ([ div [ class statusDot ] []
         , text info.statusText
         ]
            ++ (if info.showRefresh then
                    [ button
                        [ class refreshButton
                        , onClick AttemptReconnect
                        ]
                        [ text "⟳" ]
                    ]

                else
                    []
               )
        )
