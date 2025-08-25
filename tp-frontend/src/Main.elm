module Main exposing (main)

import Browser
import Html exposing (Html, div, h1, text)
import Html.Attributes exposing (class)
import Browser.Navigation
import Url

type alias Model =
    {}


type Msg
    = NoOp


init : flags -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init _ _ _ =
    ( {}, Cmd.none )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )


view : Model -> Browser.Document Msg
view model =
    { title = "ThirdPlace"
    , body =
        [ div [ class "app" ]
            [ div [ class "container" ]
                [ h1 [ class "title" ] [ text "Hello World" ]
                , div [ class "subtitle" ] [ text "Welcome to ThirdPlace" ]
                ]
            ]
        ]
    }


main : Program () Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = \_ -> Sub.none
        , onUrlChange = \_ -> NoOp
        , onUrlRequest = \_ -> NoOp
        }