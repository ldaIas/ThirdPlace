module ThirdPlaceApp exposing (init, main, subscriptions, update, view)

import Browser
import Browser.Navigation as Navigation exposing (Key)
import Identity
import LoginView exposing (view)
import RoomView exposing (view)
import ThirdPlaceModel exposing (Model, Msg(..))
import Url exposing (Url)
import Utils.Ports


init : flags -> Url -> Key -> ( Model, Cmd Msg )
init _ url key =
    let
        identityInit : ( Identity.Model, Cmd Identity.Msg )
        identityInit =
            Identity.init

        ( didModel, didCmd ) =
            identityInit

        loginModel : Model
        loginModel =
            { pageKey = key, pageUrl = url, userDid = didModel, authenticated = False }
    in
    -- Create the login model using the initialized identity and return the command to update the identity model
    ( loginModel, Cmd.map (always IdentityMsg didCmd) didCmd )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        IdentityMsg identityMsg ->
            let
                ( updatedDidModel, cmd ) =
                    Identity.update identityMsg model.userDid
            in
            -- If the identity model shows a successful login update app model to show authenticated
            if updatedDidModel.loginAuthenticated == Just True then
                ( { model | userDid = updatedDidModel, authenticated = True }, Cmd.none )

            else
                -- Otherwise, just update the model with the new identity state
                ( { model | userDid = updatedDidModel }, Cmd.map IdentityMsg cmd )

        CreateAccount ->
            let
                identityCreate : ( Identity.Model, Cmd Identity.Msg )
                identityCreate =
                    Identity.update Identity.RequestDID model.userDid

                ( didModel, cmd ) =
                    identityCreate
            in
            ( { model | userDid = didModel }, Cmd.map (always IdentityMsg cmd) cmd )

        AttemptLogin ->
            ( model, Utils.Ports.authenticate { did = model.userDid.did |> Maybe.withDefault "", privKey = model.userDid.privKey |> Maybe.withDefault "" } )

        UrlChanged _ ->
            ( model, Cmd.none )

        NoOp ->
            ( model, Cmd.none )


view : Model -> Browser.Document Msg
view model =
    { title =
        if model.authenticated then
            "ThirdPlace - Chat"

        else
            "ThirdPlace - Login"
    , body =
        if model.authenticated then
            [ RoomView.view model ]

        else
            [ LoginView.view model ]
    }


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
    Sub.map IdentityMsg (Identity.subscriptions model.userDid)
