port module SignIn exposing (Model, Msg(..), init, update, view)

import Html exposing (Html, button, div, h1, input, p, text)
import Html.Attributes exposing (class, placeholder, value)
import Html.Events exposing (onClick, onInput)
import Http
import Json.Decode as Decode


type alias Model =
    { error : Maybe String
    , isLoading : Bool
    , bypassUsername : String
    }


type Msg
    = StartFrequencySignIn
    | GotSignedRequest (Result Http.Error String)
    | SignInSuccess
    | BypassSignIn
    | UpdateBypassUsername String


init : Model
init =
    { error = Nothing
    , isLoading = False
    , bypassUsername = "current_user"
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        StartFrequencySignIn ->
            ( { model | isLoading = True, error = Nothing }, getSignedRequest )

        GotSignedRequest result ->
            case result of
                Ok signedRequest ->
                    let
                        frequencyUrl = "https://testnet.frequencyaccess.com/siwa/start?signedRequest=" ++ signedRequest
                    in
                    ( { model | isLoading = False }, redirectToFrequency frequencyUrl )

                Err _ ->
                    ( { model | isLoading = False, error = Just "Failed to get signed request" }, Cmd.none )

        SignInSuccess ->
            ( model, Cmd.none )

        BypassSignIn ->
            ( model, Cmd.none )

        UpdateBypassUsername username ->
            ( { model | bypassUsername = username }, Cmd.none )


view : Model -> Html Msg
view model =
    div [ class "signin-container" ]
        [ div [ class "signin-form" ]
            [ h1 [ class "signin-title" ] [ text "Welcome to ThirdPlace" ]
            , p [ class "signin-subtitle" ] [ text "Sign in to find people for real-world activities" ]
            , case model.error of
                Just errorMsg ->
                    div [ class "error-message" ] [ text errorMsg ]

                Nothing ->
                    text ""
            , button 
                [ onClick StartFrequencySignIn
                , class "signin-button"
                ] 
                [ text (if model.isLoading then "Connecting..." else "Sign In with Frequency") ]
            , div [ class "bypass-section" ]
                [ input
                    [ placeholder "Username for bypass"
                    , value model.bypassUsername
                    , onInput UpdateBypassUsername
                    , class "bypass-input"
                    ]
                    []
                , button 
                    [ onClick BypassSignIn
                    , class "bypass-button"
                    ] 
                    [ text "Bypass (Dev Only)" ]
                ]
            ]
        ]


getSignedRequest : Cmd Msg
getSignedRequest =
    Http.get
        { url = "http://localhost:8080/api/auth/signedRequest"
        , expect = Http.expectJson GotSignedRequest signedRequestDecoder
        }


signedRequestDecoder : Decode.Decoder String
signedRequestDecoder =
    Decode.field "signedRequest" Decode.string


port redirectToUrl : String -> Cmd msg


redirectToFrequency : String -> Cmd Msg
redirectToFrequency url =
    redirectToUrl url