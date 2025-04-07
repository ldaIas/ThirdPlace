module JSPorts.Sporran.SporranHandler exposing (..)

import JSPorts.Sporran.SporranPorts as SporranPorts

type alias Model =
    { sporranDetected : Maybe Bool
    , userDid : Maybe String
    }

type Msg =
    OnSporranDetected Bool
    | RequestLogin
    | LoginSuccess String

init : ( Model, Cmd Msg )
init =
    ( { sporranDetected = Nothing, userDid = Nothing }
    , SporranPorts.detectSporran ()
    )

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        OnSporranDetected detected ->
            ( { model | sporranDetected = Just detected }, Cmd.none )

        RequestLogin ->
            ( model, SporranPorts.requestLogin () )

        LoginSuccess did ->
            ( { model | userDid = Just did }, Cmd.none )

subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
                [ SporranPorts.onSporranDetected OnSporranDetected
                , SporranPorts.onLoginSuccess LoginSuccess
                ]