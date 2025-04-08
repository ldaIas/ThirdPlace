module JSPorts.Sporran.SporranHandler exposing (..)

import JSPorts.Sporran.SporranPorts as SporranPorts

type alias Model =
    { sporranDetected : Maybe Bool
    , userDid : Maybe String
    , attemptingLogin : Bool
    }

type Msg =
    OnSporranDetected Bool
    | RequestLogin
    | AttemptingLogin () -- Unused function param as apparently the port needs to get some kind of input from js
    | LoginSuccess String

init : ( Model, Cmd Msg )
init =
    ( { sporranDetected = Nothing, userDid = Nothing, attemptingLogin = False }
    , SporranPorts.detectSporran ()
    )

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        OnSporranDetected detected ->
            ( { model | sporranDetected = Just detected }, Cmd.none )

        RequestLogin ->
            ( model, SporranPorts.requestLogin () )

        AttemptingLogin _ ->
            ( {model | attemptingLogin = True }, Cmd.none )

        LoginSuccess did ->
            ( { model | userDid = Just did }, Cmd.none )

subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
                [ SporranPorts.onSporranDetected OnSporranDetected
                , SporranPorts.onLoginSuccess LoginSuccess
                , SporranPorts.onLoginAttempted AttemptingLogin
                ]