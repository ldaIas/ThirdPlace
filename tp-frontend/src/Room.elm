module Room exposing (..)

import Browser
import Browser.Navigation as Navigation
import Html exposing (Html, button, div, input, text)
import Html.Attributes exposing (style)
import Html.Events exposing (on, onClick)
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
    , messages = []
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
        [ div [ style "display" "flex", style "height" "100vh", style "background-color" "lightgray", style "justify-content" "center", onClick DeselectConversation ]
            [ div [ style "width" "60vw", style "display" "flex", style "justify-content" "center", style "align-items" "center" ]
                [ div [ style "width" "1000px", style "height" "500px", style "background-image" "radial-gradient(circle, #02020d, #353544)", style "border-radius" "50%", style "display" "flex", style "flex-direction" "column", style "align-items" "center", style "justify-content" "center" ]
                    [ div [ style "position" "relative", style "left" "-11vw", style "top" "-3vh" ]
                        [ conversationBubble "Hello" 1
                        , conversationBubble "How are you?" 0.6
                        , conversationBubble "I'm doing great!" 0.3
                        , conversationBubble "..." 0.1
                        ]
                    , div [ style "position" "relative", style "left" "15vw", style "top" "5vh" ]
                        [ conversationBubble "MEOW" 1
                        , conversationBubble "MEOW" 0.6
                        , conversationBubble "MEOW" 0.3
                        , conversationBubble "..." 0.1
                        ]
                    ]
                ]
            , case model.selectedConversation of
                Just conversation ->
                    div [ style "width" "40vw", style "height" "100vh", style "position" "absolute", style "right" "0", style "top" "0", style "background-color" "white" ]
                        [ div [] [ text ("Full conversation: " ++ conversation) ] ]

                Nothing ->
                    div [] []
            , div [ style "position" "absolute", style "bottom" "0", style "width" "80vw", style "height" "20vh", style "background-color" "white", style "border-top" "1px solid black", style "display" "flex", style "align-items" "center", style "justify-content" "center" ]
                [ Html.input [ style "width" "90%", style "height" "50%", style "border" "1px solid gray", style "border-radius" "5px" ] [] ]
            ]
        ]
    }


conversationBubble : String -> Float -> Html Msg
conversationBubble message opacity =
    div [ style "background-color" "lightgray", style "padding" "10px", style "border-radius" "10px", style "margin-bottom" "5px", style "opacity" (String.fromFloat opacity), onClick (Debug.log "clicked" (SelectConversation message)) ]
        [ text message ]
