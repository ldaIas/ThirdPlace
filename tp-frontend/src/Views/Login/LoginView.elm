module Views.Login.LoginView exposing (view)

import Html exposing (Html, button, div, h1, p, text)
import Html.Attributes exposing (class)
import Views.Login.LoginStyles exposing (accountPane, createContainer, fieldsContainer, loginContainer, logoBody, separator, loginView)
import Html.Events exposing (onClick)
import ThirdPlaceModel exposing (Model, Msg(..))
import JSPorts.Identity.IdentityHandler as IdentityHandler
import Html exposing (input)
import Html.Attributes exposing (type_)
import Html.Attributes exposing (placeholder)

view : Model -> Html Msg
view model =
    div [ class loginView ]
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
                    ([ displayDid model.userDid
                     , button [ onClick AttemptLogin, Html.Attributes.disabled (model.userDid.did == Nothing) ] [ text "Login" ]
                     ]
                        ++ (if model.userDid.loginAuthenticated == Just False then
                                [ p [] [ text "Login failed. Please try again." ] ]

                            else
                                []
                           )
                    )
                ]
            ]
        ]

displayDid : IdentityHandler.Model -> Html Msg
displayDid maybeDid =
    case maybeDid.did of
        Just did ->
            input [ type_ "text", placeholder ("User DID: " ++ did) ] []

        Nothing ->
            input [ type_ "text", placeholder "No DID" ] []