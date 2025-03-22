module Login exposing (..)

import Browser
import Browser.Navigation as Navigation exposing (Key)
import Html exposing (Html, button, div, h1, input, p, text)
import Html.Attributes exposing (class, placeholder, type_)
import Html.Events exposing (onClick, onInput)
import Identity
import LoginStyles exposing (accountPane, createContainer, fieldsContainer, loginContainer, logoBody, separator)
import Url exposing (Url)


{-| The model for the login page. It contains the page key, url, and user DID.
-| userDid is initialized using the Identity module and starts off with Nothing.
-}
type alias Model =
    { pageKey : Key
    , pageUrl : Url
    , userDid : Identity.Model
    }


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
            { pageKey = key, pageUrl = url, userDid = didModel }
    in
    -- Create the login model using the initialized identity and return the command to update the identity model
    ( loginModel, Cmd.map (always IdentityMsg didCmd) didCmd )


type Msg
    = CreateAccount
    | Login
    | IdentityMsg Identity.Msg
    | UrlChanged Url
    | NoOp


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        IdentityMsg identityMsg ->
            let
                ( updatedDidModel, cmd ) =
                    Identity.update identityMsg model.userDid
            in
            ( { model | userDid = updatedDidModel }, Cmd.map IdentityMsg cmd )

        CreateAccount ->
            let
                identityCreate : ( Identity.Model, Cmd Identity.Msg )
                identityCreate =
                    Identity.update Identity.RequestDID model.userDid

                ( didModel, cmd ) =
                    identityCreate
            in
            -- Navigate to the Room page
            ( { model | userDid = didModel }, Cmd.map (always IdentityMsg cmd) cmd )

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
                    [ button [ onClick CreateAccount, Html.Attributes.disabled (model.userDid.did /= Nothing) ] [ text "Create Account" ]
                    ]
                , div [ class separator ] []
                , div [ class loginContainer ]
                    [ displayDid model.userDid
                    , button [ onClick Login ] [ text "Login" ]
                    ]
                ]
            ]
        ]
    }


displayDid : Identity.Model -> Html Msg
displayDid maybeDid =
    case maybeDid.did of
        Just did ->
            input [ type_ "text", placeholder ("User DID: " ++ did) ] []

        Nothing ->
            input [ type_ "text", placeholder "No DID" ] []


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


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.map IdentityMsg (Identity.subscriptions model.userDid)
