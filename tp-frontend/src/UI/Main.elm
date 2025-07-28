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
    , orbitDBStatus : String
    , databaseAddress : Maybe String
    , dataHash : Maybe String
    , allData : Maybe String
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
      , orbitDBStatus = "Not initialized"
      , databaseAddress = Nothing
      , dataHash = Nothing
      , allData = Nothing
      }
    , Cmd.none
    )


-- UPDATE


-- PORTS


port ipfsStatusChanged : (String -> msg) -> Sub msg


port publishTestContent : () -> Cmd msg


port contentPublished : (String -> msg) -> Sub msg


port contentRetrieved : (String -> msg) -> Sub msg


port orbitDBStatusChanged : (String -> msg) -> Sub msg


port createTestDatabase : () -> Cmd msg


port databaseCreated : (String -> msg) -> Sub msg


port addTestData : () -> Cmd msg


port dataAdded : (String -> msg) -> Sub msg


port retrieveAllData : () -> Cmd msg


port allDataRetrieved : (String -> msg) -> Sub msg


-- MESSAGES


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | PublishTestContent
    | IpfsStatusChanged String
    | ContentPublished String
    | ContentRetrieved String
    | OrbitDBStatusChanged String
    | CreateTestDatabase
    | DatabaseCreated String
    | AddTestData
    | DataAdded String
    | RetrieveAllData
    | AllDataRetrieved String


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

        OrbitDBStatusChanged status ->
            ( { model | orbitDBStatus = status }, Cmd.none )

        CreateTestDatabase ->
            ( model, createTestDatabase () )

        DatabaseCreated address ->
            ( { model | databaseAddress = Just address }, Cmd.none )

        AddTestData ->
            ( model, addTestData () )

        DataAdded hash ->
            ( { model | dataHash = Just hash }, Cmd.none )

        RetrieveAllData ->
            ( model, retrieveAllData () )

        AllDataRetrieved data ->
            ( { model | allData = Just data }, Cmd.none )


-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.batch
        [ ipfsStatusChanged IpfsStatusChanged
        , contentPublished ContentPublished
        , contentRetrieved ContentRetrieved
        , orbitDBStatusChanged OrbitDBStatusChanged
        , databaseCreated DatabaseCreated
        , dataAdded DataAdded
        , allDataRetrieved AllDataRetrieved
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
                , div [ class "status" ]
                    [ text model.orbitDBStatus ]
                , div [ class "section" ]
                    [ button
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
                , div [ class "section" ]
                    [ button
                        [ class "publish-btn"
                        , onClick CreateTestDatabase
                        ]
                        [ text "Create Test Database" ]
                    , case model.databaseAddress of
                        Nothing ->
                            text ""
                        
                        Just address ->
                            div [ class "result" ]
                                [ div [ class "address" ]
                                    [ text ("Database: " ++ address) ]
                                ]
                    ]
                , div [ class "section" ]
                    [ button
                        [ class "publish-btn"
                        , onClick AddTestData
                        ]
                        [ text "Add Test Data" ]
                    , case model.dataHash of
                        Nothing ->
                            text ""
                        
                        Just hash ->
                            div [ class "result" ]
                                [ div [ class "hash" ]
                                    [ text ("Data Hash: " ++ hash) ]
                                ]
                    ]
                , div [ class "section" ]
                    [ button
                        [ class "publish-btn"
                        , onClick RetrieveAllData
                        ]
                        [ text "Retrieve All Data" ]
                    , case model.allData of
                        Nothing ->
                            text ""
                        
                        Just data ->
                            div [ class "result" ]
                                [ div [ class "data" ]
                                    [ text ("All Data: " ++ data) ]
                                ]
                    ]
                ]
            ]
        ]
    }