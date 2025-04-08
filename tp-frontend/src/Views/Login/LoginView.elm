module Views.Login.LoginView exposing (view)

import Html exposing (Html, a, button, div, h1, p, text)
import Html.Attributes exposing (class, href)
import Html.Events exposing (onClick)
import JSPorts.Sporran.SporranHandler as SporranHandler
import ThirdPlaceModel exposing (Model, Msg(..))
import Views.Login.LoginStyles exposing (accountPane, fieldsContainer, loginView, logoBody)


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
            p [] [ text "Checking for Sporran..." ]

        Just False ->
            div [] [ p [] [ text "Sporran wallet needed for identity. " ], a [ href "https://www.sporran.org/" ] [ text "Get it here" ] ]

        Just True ->
            case model.userDid of
                Just did ->
                    p [] [ text ("Logged in with DID: " ++ did) ]

                Nothing ->
                    if not model.attemptingLogin then
                        button [ onClick (SporranMsg SporranHandler.RequestLogin) ] [ text "Login with Sporran ID" ]

                    else
                        p [] [ text "Contacting the spiritnet chain to log in..." ]
