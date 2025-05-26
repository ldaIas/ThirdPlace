module JSPorts.Sporran.SporranHandler exposing (..)

import JSPorts.Geohash.GeohashPorts as GeohashPorts
import JSPorts.Sporran.SporranPorts as SporranPorts


type alias Model =
    { sporranDetected : Maybe Bool
    , userDid : Maybe String
    , attemptingLogin : Bool
    }


type Msg
    = OnSporranDetected Bool
    | CheckForSporran
    | RequestLogin
    | AttemptingLogin () -- Unused function param as apparently the port needs to get some kind of input from js
    | LoginSuccess String


init : ( Model, Cmd Msg )
init =
    ( { sporranDetected = Nothing, userDid = Nothing, attemptingLogin = False }
    , SporranPorts.detectSporran ()
      -- Automatically try to detect sporran at start
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        OnSporranDetected detected ->
            ( { model | sporranDetected = Just detected }, Cmd.none )

        CheckForSporran ->
            ( { model | sporranDetected = Nothing }, SporranPorts.detectSporran () )

        -- Authenticate the DID, as well as requesting the room geohash id
        RequestLogin ->
            let
                authRequest =
                    SporranPorts.requestLogin ()

                roomIdRequest =
                    Debug.log "requesting room hash" GeohashPorts.requestRoomId ()
            in
            ( model, Cmd.batch [ authRequest, roomIdRequest ] )

        AttemptingLogin _ ->
            ( { model | attemptingLogin = True }, Cmd.none )

        LoginSuccess did ->
            ( { model | userDid = Just did }, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ SporranPorts.onSporranDetected OnSporranDetected
        , SporranPorts.onLoginSuccess LoginSuccess
        , SporranPorts.onLoginAttempted AttemptingLogin
        ]
