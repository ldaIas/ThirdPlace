module Main exposing (main)

import Browser
import Browser.Navigation
import Html exposing (Html, button, div, h1, p, text)
import Html.Attributes exposing (class)
import Html.Events exposing (onClick)
import Posts
import SignIn
import Url


type alias Model =
    { postsModel : Posts.Model
    , signInModel : SignIn.Model
    , isSignedIn : Bool
    }


type Msg
    = NoOp
    | PostsMsg Posts.Msg
    | SignInMsg SignIn.Msg
    | SignOut


init : flags -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init _ _ _ =
    ( { postsModel = Posts.init
      , signInModel = SignIn.init
      , isSignedIn = False
      }, Cmd.none )





update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )

        PostsMsg postsMsg ->
            let
                ( updatedPostsModel, postsCmd ) =
                    Posts.update postsMsg model.postsModel
            in
            ( { model | postsModel = updatedPostsModel }, Cmd.map PostsMsg postsCmd )

        SignInMsg signInMsg ->
            case signInMsg of
                SignIn.SubmitSignIn ->
                    let
                        ( updatedSignInModel, signInCmd ) =
                            SignIn.update signInMsg model.signInModel
                    in
                    ( { model | signInModel = updatedSignInModel, isSignedIn = True }, Cmd.batch [ Cmd.map SignInMsg signInCmd, Cmd.map PostsMsg Posts.loadPosts ] )

                _ ->
                    let
                        ( updatedSignInModel, signInCmd ) =
                            SignIn.update signInMsg model.signInModel
                    in
                    ( { model | signInModel = updatedSignInModel }, Cmd.map SignInMsg signInCmd )

        SignOut ->
            ( { model | isSignedIn = False, signInModel = SignIn.init }, Cmd.none )


view : Model -> Browser.Document Msg
view model =
    { title = "ThirdPlace"
    , body =
        if model.isSignedIn then
            [ div [ class "app" ]
                [ div [ class "header" ]
                    [ h1 [ class "app-title" ] [ text "ThirdPlace" ]
                    , div [ class "header-actions" ]
                        [ p [ class "app-subtitle" ] [ text "Find people to join you for real-world activities" ]
                        , button [ class "signout-button", onClick SignOut ] [ text "Sign Out" ]
                        ]
                    ]
                , Html.map PostsMsg (Posts.view model.postsModel)
                ]
            ]
        else
            [ Html.map SignInMsg (SignIn.view model.signInModel) ]
    }



main : Program () Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = \_ -> Sub.none
        , onUrlChange = \_ -> NoOp
        , onUrlRequest = \_ -> NoOp
        }
