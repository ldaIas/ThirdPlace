module ThirdPlaceApp exposing (init, main, subscriptions, update, view)

import Browser
import Browser.Navigation as Navigation
import JSPorts.Geohash.GeohashHandler as GeohashHandler
import JSPorts.Identity.IdentityHandler as IdentityHandler
import JSPorts.Identity.IdentityPorts as IdentityPorts
import JSPorts.Sporran.SporranHandler as SporranHandler
import JSPorts.WebRTC.WebRTCHandler as WebRTCHandler
import ThirdPlaceModel exposing (Model, Msg(..))
import Url exposing (Url)
import Views.Login.LoginView exposing (view)
import Views.Room.RoomHandler as RoomHandler
import Views.Room.RoomModel as RoomModel
import Views.Room.RoomView exposing (view)
import Views.ThirdPlaceAppView
import JSPorts.Geohash.GeohashPorts as GeohashPorts


init : flags -> Url -> Navigation.Key -> ( Model, Cmd Msg )
init _ url key =
    let
        identityInit : ( IdentityHandler.Model, Cmd IdentityHandler.Msg )
        identityInit =
            IdentityHandler.init

        sporranInit : ( SporranHandler.Model, Cmd SporranHandler.Msg )
        sporranInit =
            SporranHandler.init

        geohashInit : ( GeohashHandler.Model, Cmd GeohashHandler.Msg )
        geohashInit =
            GeohashHandler.init

        roomInit : ( RoomModel.Model, Cmd RoomModel.Msg )
        roomInit =
            RoomHandler.init

        webRtcHandlerInit : ( WebRTCHandler.Model, Cmd WebRTCHandler.Msg )
        webRtcHandlerInit =
            -- Initialize the WebRTC handler, this will be used for P2P communication in the chat room
            WebRTCHandler.init

        ( webRtcHandler, webRtcCmd ) =
            webRtcHandlerInit

        ( didModel, didCmd ) =
            identityInit

        ( sporranModel, sporranCmd ) =
            sporranInit

        ( geohashModel, geohashCmd ) =
            geohashInit

        ( roomModel, roomCmd ) =
            roomInit

        loginModel : Model
        loginModel =
            { pageKey = key
            , pageUrl = url
            , userDid = didModel
            , authenticated = False
            , webRtcHandler = webRtcHandler
            , sporranHandler = sporranModel
            , geohashHandler = geohashModel
            , roomHandler = roomModel
            }
    in
    ( loginModel
    , Cmd.batch
        [ Cmd.map IdentityMsg didCmd
        , Cmd.map SporranMsg sporranCmd
        , Cmd.map GeohashMsg geohashCmd
        , Cmd.map WebRTCMsg webRtcCmd
        , Cmd.map RoomMsg roomCmd
        ]
    )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        IdentityMsg identityMsg ->
            let
                ( updatedDidModel, cmd ) =
                    IdentityHandler.update identityMsg model.userDid
            in
            -- If the identity model shows a successful login update app model to show authenticated
            if updatedDidModel.loginAuthenticated == Just True then
                ( { model | userDid = updatedDidModel, authenticated = True }, Cmd.none )

            else
                -- Otherwise, just update the model with the new identity state
                ( { model | userDid = updatedDidModel }, Cmd.map IdentityMsg cmd )

        SporranMsg sporranMsg ->
            let
                ( updatedSporranModel, cmd ) =
                    SporranHandler.update sporranMsg model.sporranHandler

                -- If the sporran model has a user did, we are authenticated
                authenticated : Bool
                authenticated =
                    case updatedSporranModel.userDid of
                        Just _ ->
                            True

                        Nothing ->
                            False
            in
            ( { model | sporranHandler = updatedSporranModel, authenticated = authenticated }, Cmd.map SporranMsg cmd )

        GeohashMsg geohashMsg ->
            let
                ( updatedGeohashModel, cmd ) =
                    GeohashHandler.update model.geohashHandler geohashMsg

                -- Update the room model to have the new room id
                updatedRoomModel =
                    let
                        roomModel = model.roomHandler
                    in
                    { roomModel | roomId = updatedGeohashModel.roomId}
            in
            ( { model | geohashHandler = updatedGeohashModel, roomHandler = updatedRoomModel }, Cmd.map GeohashMsg cmd )

        RoomMsg roomMsg ->
            let
                ( updatedRoomModel, cmd ) =
                    RoomHandler.update roomMsg model.roomHandler
            in
            ( { model | roomHandler = updatedRoomModel }, Cmd.map RoomMsg cmd )

        WebRTCMsg webRtcMsg ->
            let
                ( updatedWebRtcModel, cmd ) =
                    WebRTCHandler.update webRtcMsg model.webRtcHandler
            in
            ( { model | webRtcHandler = updatedWebRtcModel }, Cmd.map WebRTCMsg cmd )

        CreateAccount ->
            let
                identityCreate : ( IdentityHandler.Model, Cmd IdentityHandler.Msg )
                identityCreate =
                    IdentityHandler.update IdentityHandler.RequestDID model.userDid

                ( didModel, cmd ) =
                    identityCreate
            in
            ( { model | userDid = didModel }, Cmd.map (always IdentityMsg cmd) cmd )

        AttemptLogin ->
            let 
                identityAuth = 
                    IdentityPorts.authenticate { did = model.userDid.did |> Maybe.withDefault "", privKey = model.userDid.privKey |> Maybe.withDefault "" }
            in
            ( model, identityAuth )

        UrlChanged _ ->
            ( model, Cmd.none )

        NoOp ->
            ( model, Cmd.none )


view : Model -> Browser.Document Msg
view model =
    Views.ThirdPlaceAppView.view model


main : Program () Model Msg
main =
    Browser.application
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = \_ -> NoOp
        }



{-
   -| Subscriptions for the various events in the app
   -| These are things like user DID generation and authentication
-}


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ Sub.map IdentityMsg (IdentityHandler.subscriptions model.userDid)
        , Sub.map SporranMsg (SporranHandler.subscriptions model.sporranHandler)
        , Sub.map WebRTCMsg (WebRTCHandler.subscriptions model.webRtcHandler)
        , Sub.map GeohashMsg (GeohashHandler.subscriptions model.geohashHandler)
        ]
