module Views.Login.LoginView exposing (view)

import Html exposing (Html, button, div, h1, p, text)
import Html.Attributes exposing (class)
import Views.Login.LoginStyles exposing (accountPane, createContainer, fieldsContainer, loginContainer, logoBody, separator, loginView)
import Html.Events exposing (onClick)
import ThirdPlaceModel exposing (Model, Msg(..))
import JSPorts.Identity.IdentityHandler as IdentityHandler
import JSPorts.Sporran.SporranHandler as SporranHandler
import Html exposing (input)
import Html.Attributes exposing (type_)
import Html.Attributes exposing (placeholder)
import Html exposing (a)
import Html.Attributes exposing (href)

view : Model -> Html Msg
view model =
    div [ class loginView ]
        [ div [ class logoBody ]
            [ p [] [ text "ThirdPlace" ] ]
        , div [ class accountPane ]
            [ h1 [] [ text "🏢💁\u{200D}♀️💬" ]
            , div [ class fieldsContainer ]
                [ displaySporranDetection model.sporranHandler
                ]
            ]
        ]

displaySporranDetection : SporranHandler.Model -> Html Msg
displaySporranDetection model =
    case model.sporranDetected of
        Nothing ->
            p [] [text "Checking for Sporran..."]

        Just False -> 
            div [] [p [] [text "Sporran wallet needed for identity. "], a [href "https://www.sporran.org/"] [text "Get it here"]]
        
        Just True ->
            case model.userDid of
                Just did ->
                    p [] [text ("Logged in with DID: " ++ did)]

                Nothing ->
                    button [onClick (SporranMsg SporranHandler.RequestLogin)] [text "Login with Sporran ID"]

displayDid : IdentityHandler.Model -> Html Msg
displayDid maybeDid =
    case maybeDid.did of
        Just did ->
            input [ type_ "text", placeholder ("User DID: " ++ did) ] []

        Nothing ->
            input [ type_ "text", placeholder "No DID" ] []