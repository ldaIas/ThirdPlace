module SignIn exposing (Model, Msg(..), init, update, view)

import Html exposing (Html, button, div, form, h1, input, p, text)
import Html.Attributes exposing (class, placeholder, type_, value)
import Html.Events exposing (onClick, onInput, onSubmit, preventDefaultOn)
import Json.Decode as Decode


type alias Model =
    { username : String
    , password : String
    , error : Maybe String
    }


type Msg
    = UpdateUsername String
    | UpdatePassword String
    | SubmitSignIn
    | SignInSuccess


init : Model
init =
    { username = ""
    , password = ""
    , error = Nothing
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateUsername username ->
            ( { model | username = username }, Cmd.none )

        UpdatePassword password ->
            ( { model | password = password }, Cmd.none )

        SubmitSignIn ->
            if String.isEmpty (String.trim model.username) || String.isEmpty (String.trim model.password) then
                ( { model | error = Just "Please enter both username and password" }, Cmd.none )
            else
                ( { model | error = Nothing }, Cmd.none )

        SignInSuccess ->
            ( model, Cmd.none )


view : Model -> Html Msg
view model =
    div [ class "signin-container" ]
        [ div [ class "signin-form" ]
            [ h1 [ class "signin-title" ] [ text "Welcome to ThirdPlace" ]
            , p [ class "signin-subtitle" ] [ text "Sign in to find people for real-world activities" ]
            , form [ onSubmit SubmitSignIn, preventDefaultOn "submit" (Decode.succeed ( SubmitSignIn, True )) ]
                [ div [ class "form-group" ]
                    [ input
                        [ type_ "text"
                        , placeholder "Username"
                        , value model.username
                        , onInput UpdateUsername
                        , class "form-input"
                        ]
                        []
                    ]
                , div [ class "form-group" ]
                    [ input
                        [ type_ "password"
                        , placeholder "Password"
                        , value model.password
                        , onInput UpdatePassword
                        , class "form-input"
                        ]
                        []
                    ]
                , case model.error of
                    Just errorMsg ->
                        div [ class "error-message" ] [ text errorMsg ]

                    Nothing ->
                        text ""
                , button [ type_ "submit", class "signin-button" ] [ text "Sign In" ]
                ]
            ]
        ]