port module UI.Main exposing (main)

import Browser
import Browser.Navigation as Nav
import Html exposing (Html, button, div, text)
import Html.Attributes exposing (class)
import Html.Events exposing (onClick)
import Json.Decode as Decode
import Json.Encode as Encode
import Url


-- MAIN


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = LinkClicked
        }


-- MODEL


type alias Model =
    { key : Nav.Key
    , url : Url.Url
    , ipfsStatus : String
    , publishedCid : Maybe String
    , retrievedContent : Maybe String
    }


type alias Flags =
    {}


init : Flags -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    ( { key = key
      , url = url
      , ipfsStatus = "Connecting..."
      , publishedCid = Nothing
      , retrievedContent = Nothing
      }
    , Cmd.none
    )


-- UPDATE


-- PORTS


port ipfsStatusChanged : (String -> msg) -> Sub msg


port publishTestContent : () -> Cmd msg


port contentPublished : (String -> msg) -> Sub msg


port contentRetrieved : (String -> msg) -> Sub msg


-- MESSAGES


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | PublishTestContent
    | IpfsStatusChanged String
    | ContentPublished String
    | ContentRetrieved String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Nav.pushUrl model.key (Url.toString url) )

                Browser.External href ->
                    ( model, Nav.load href )

        UrlChanged url ->
            ( { model | url = url }
            , Cmd.none
            )

        PublishTestContent ->
            ( model, publishTestContent () )

        IpfsStatusChanged status ->
            ( { model | ipfsStatus = status }, Cmd.none )

        ContentPublished cid ->
            ( { model | publishedCid = Just cid }, Cmd.none )

        ContentRetrieved content ->
            ( { model | retrievedContent = Just content }, Cmd.none )


-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ ipfsStatusChanged IpfsStatusChanged
        , contentPublished ContentPublished
        , contentRetrieved ContentRetrieved
        ]


-- VIEW


view : Model -> Browser.Document Msg
view model =
    { title = "ThirdPlace"
    , body =
        [ div [ class "app" ]
            [ div [ class "container" ]
                [ div [ class "status" ]
                    [ text model.ipfsStatus ]
                , button
                    [ class "publish-btn"
                    , onClick PublishTestContent
                    ]
                    [ text "Publish Test Content" ]
                , case model.publishedCid of
                    Nothing ->
                        text ""
                    
                    Just cid ->
                        div [ class "result" ]
                            [ div [ class "cid" ]
                                [ text ("Published CID: " ++ cid) ]
                            ]
                , case model.retrievedContent of
                    Nothing ->
                        text ""
                    
                    Just content ->
                        div [ class "result" ]
                            [ div [ class "content" ]
                                [ text ("Retrieved: " ++ content) ]
                            ]
                ]
            ]
        ]
    }