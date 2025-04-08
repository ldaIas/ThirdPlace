module ThirdPlaceApp exposing (init, main, subscriptions, update, view)

import Browser
import Browser.Navigation as Navigation
import JSPorts.Identity.IdentityHandler as IdentityHandler
import JSPorts.Identity.IdentityPorts as IdentityPorts
import JSPorts.Sporran.SporranHandler as SporranHandler
import JSPorts.Sporran.SporranPorts as SporranPorts
import JSPorts.WebRTC.WebRTCHandler as WebRTCHandler
import ThirdPlaceModel exposing (Model, Msg(..))
import Url exposing (Url)
import Views.Login.LoginView as LoginView exposing (view)
import Views.Room.RoomView as RoomView exposing (view)
import Views.ThirdPlaceAppView


init : flags -> Url -> Navigation.Key -> ( Model, Cmd Msg )
init _ url key =
    let
        identityInit : ( IdentityHandler.Model, Cmd IdentityHandler.Msg )
        identityInit =
            IdentityHandler.init

        sporranInit : ( SporranHandler.Model, Cmd SporranHandler.Msg )
        sporranInit =
            SporranHandler.init

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

        loginModel : Model
        loginModel =
            { pageKey = key, pageUrl = url, userDid = didModel, authenticated = False, webRtcHandler = webRtcHandler, sporranHandler = sporranModel }
    in
    ( loginModel
    , Cmd.batch
        [ Cmd.map IdentityMsg didCmd
        , Cmd.map SporranMsg sporranCmd
        , Cmd.map WebRTCMsg webRtcCmd
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
                authenticated: Bool
                authenticated = 
                    case updatedSporranModel.userDid of
                        Just _ -> True
                        Nothing -> False
            in
            ( { model | sporranHandler = updatedSporranModel, authenticated = authenticated }, Cmd.map SporranMsg cmd )

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
            ( model, IdentityPorts.authenticate { did = model.userDid.did |> Maybe.withDefault "", privKey = model.userDid.privKey |> Maybe.withDefault "" } )

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
        ]
