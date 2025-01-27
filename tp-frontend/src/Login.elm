module Login exposing (..)

import Browser
import Browser.Navigation as Navigation exposing (Key)
import Html exposing (Html, button, div, h1, input, p, text)
import Html.Attributes exposing (class, placeholder, type_)
import Html.Events exposing (onClick, onInput)
import LoginStyles exposing (accountPane, loginContainer, logoBody)
import Url exposing (Url)
import LoginStyles exposing (createContainer)
import LoginStyles exposing (fieldsContainer)
import LoginStyles exposing (separator)


type alias Model =
    { username : String
    , password : String
    , pageKey : Key
    , pageUrl : Url
    }


init : flags -> Url -> Key -> ( Model, Cmd Msg )
init _ url key =
    ( { username = "", password = "", pageKey = key, pageUrl = url }, Cmd.none )


type Msg
    = UpdateUsername String
    | UpdatePassword String
    | Login
    | UrlChanged Url
    | NoOp


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateUsername newUsername ->
            ( { model | username = newUsername }, Cmd.none )

        UpdatePassword newPassword ->
            ( { model | password = newPassword }, Cmd.none )

        Login ->
            -- Navigate to the Room page
            ( model, Navigation.load "/room.html" )

        UrlChanged url ->
            ( model, Cmd.none )

        NoOp ->
            ( model, Cmd.none )


view : Model -> Browser.Document Msg
view model =
    { title = "ThirdPlace Login"
    , body =
        [ div [ class logoBody ]
            [ p [] [ text "ThirdPlace" ] ]
        , div [ class accountPane ]
            [ h1 [] [ text "🏢💁\u{200D}♀️💬" ]
            , div [ class fieldsContainer ]
                [ div [ class createContainer ]
                    [ input [ type_ "text", placeholder "Email", onInput UpdateUsername ] []
                    , input [ type_ "text", placeholder "Username", onInput UpdateUsername ] []
                    , input [ type_ "password", placeholder "Password", onInput UpdatePassword ] []
                    , input [ type_ "password", placeholder "Re-type password", onInput UpdatePassword ] []
                    , button [ onClick Login ] [ text "Create Account" ]
                    ]
                , div [ class separator ] []
                , div [ class loginContainer ]
                    [ input [ type_ "text", placeholder "Username", onInput UpdateUsername ] []
                    , input [ type_ "password", placeholder "Password", onInput UpdatePassword ] []
                    , button [ onClick Login ] [ text "Login" ]
                    ]
                ]
            ]
        ]
    }


main : Program () Model Msg
main =
    Browser.application
        { init = init
        , update = update
        , view = view
        , subscriptions = \_ -> Sub.none
        , onUrlChange = UrlChanged
        , onUrlRequest = \_ -> NoOp
        }
