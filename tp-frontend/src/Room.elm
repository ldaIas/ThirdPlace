module Room exposing (..)

import Browser
import Browser.Navigation as Navigation
import Html exposing (Html, button, div, input, p, text)
import Html.Attributes exposing (style, class)
import Html.Events exposing (on, onClick)
import RoomStyles exposing (..)
import RoomUtils
import Task
import Time
import Url exposing (Url)
import Utils.Ports
import Websockets exposing (WebsocketMessage)


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
    , messages = [ { username = "test", message = "asdw", conversationId = "54252" } ]
    , time = Time.millisToPosix 0
    }


type Msg
    = SelectConversation String
    | DeselectConversation
    | UrlChanged Url
    | NoOp
    | GotMessage String
    | SendMessage RoomUtils.ChatMessage
    | SocketOpened
    | SocketMessage WebsocketMessage
    | SocketClosed
    | GotTime Time.Posix
    | GotTimeMs Int


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

        SocketOpened ->
            ( { model | connected = Open }, Cmd.none )

        SocketClosed ->
            ( { model | connected = Closed }, Cmd.none )

        SocketMessage { data } ->
            case RoomUtils.decodeMessage data of
                Ok message ->
                    ( { model | messages = model.messages ++ [ message ] }
                    , Cmd.none
                    )

                _ ->
                    ( model, Cmd.none )


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
        , Utils.Ports.socket.open "chat" "ws://localhost:8080/" []
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
                        [ conversationBubble "MEOW" 1
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
                [ div [ class messagesContainer ] [ viewMessages model.messages ]
                , Html.input [ class chatInput ] []
                ]
            ]
        ]
    }


viewMessages : List RoomUtils.ChatMessage -> Html msg
viewMessages messages =
    div []
        (List.map viewMessage messages)


viewMessage : RoomUtils.ChatMessage -> Html msg
viewMessage message =
    p [] [ text message.message ]


conversationBubble : String -> Float -> Html Msg
conversationBubble message opacity =
    div [ class RoomStyles.conversationBubble, style "opacity" (String.fromFloat opacity) ]
        [ text message ]
