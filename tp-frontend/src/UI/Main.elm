port module UI.Main exposing (main)

import Browser
import Browser.Navigation as Nav
import Html exposing (Html, button, div, text, input, textarea, select, option, label, h2)
import Html.Attributes exposing (class, value, placeholder, type_, checked, disabled)
import Html.Events exposing (onClick, onInput, onCheck)
import String
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
    , posts : Maybe String
    -- Post creation form fields
    , postTitle : String
    , postDescription : String
    , postLocation : String
    , postGroupSize : String
    , postCategory : String
    , postIsDate : Bool
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
      , posts = Nothing
      -- Initialize form fields
      , postTitle = ""
      , postDescription = ""
      , postLocation = ""
      , postGroupSize = "2"
      , postCategory = "coffee"
      , postIsDate = False
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


port submitPost : Encode.Value -> Cmd msg


port viewPosts : () -> Cmd msg


port postsRetrieved : (String -> msg) -> Sub msg


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
    -- Post creation form messages
    | UpdatePostTitle String
    | UpdatePostDescription String
    | UpdatePostLocation String
    | UpdatePostGroupSize String
    | UpdatePostCategory String
    | TogglePostIsDate
    | SubmitPost
    | ViewPosts
    | PostsRetrieved String


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

        UpdatePostTitle title ->
            ( { model | postTitle = title }, Cmd.none )

        UpdatePostDescription description ->
            ( { model | postDescription = description }, Cmd.none )

        UpdatePostLocation location ->
            ( { model | postLocation = location }, Cmd.none )

        UpdatePostGroupSize size ->
            ( { model | postGroupSize = size }, Cmd.none )

        UpdatePostCategory category ->
            ( { model | postCategory = category }, Cmd.none )

        TogglePostIsDate ->
            ( { model | postIsDate = not model.postIsDate }, Cmd.none )

        SubmitPost ->
            if String.trim model.postTitle == "" || String.trim model.postLocation == "" then
                -- Don't submit if required fields are empty
                ( model, Cmd.none )
            else
                let
                    postData = Encode.object
                        [ ( "title", Encode.string (String.trim model.postTitle) )
                        , ( "description", Encode.string (String.trim model.postDescription) )
                        , ( "location", Encode.string (String.trim model.postLocation) )
                        , ( "groupSize", Encode.int (String.toInt model.postGroupSize |> Maybe.withDefault 2) )
                        , ( "category", Encode.string model.postCategory )
                        , ( "isDateActivity", Encode.bool model.postIsDate )
                        ]
                in
                ( { model | postTitle = "", postDescription = "", postLocation = "", postGroupSize = "2" }, submitPost postData )

        ViewPosts ->
            ( model, viewPosts () )

        PostsRetrieved posts ->
            ( { model | posts = Just posts }, Cmd.none )


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
        , postsRetrieved PostsRetrieved
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
                , postCreationForm model
                , postsDisplaySection model
                ]
            ]
        ]
    }


postCreationForm : Model -> Html Msg
postCreationForm model =
    div [ class "post-form-section" ]
        [ h2 [] [ text "Create Activity Post" ]
        , div [ class "form-group" ]
            [ label [] [ text "Title" ]
            , input
                [ type_ "text"
                , value model.postTitle
                , placeholder "e.g., Coffee at Blue Bottle"
                , onInput UpdatePostTitle
                ]
                []
            ]
        , div [ class "form-group" ]
            [ label [] [ text "Description (optional)" ]
            , textarea
                [ value model.postDescription
                , placeholder "Additional details about the activity..."
                , onInput UpdatePostDescription
                ]
                []
            ]
        , div [ class "form-group" ]
            [ label [] [ text "Location" ]
            , input
                [ type_ "text"
                , value model.postLocation
                , placeholder "e.g., Blue Bottle Coffee, 123 Main St"
                , onInput UpdatePostLocation
                ]
                []
            ]
        , div [ class "form-group" ]
            [ label [] [ text "Group Size" ]
            , input
                [ type_ "number"
                , value model.postGroupSize
                , onInput UpdatePostGroupSize
                ]
                []
            ]
        , div [ class "form-group" ]
            [ label [] [ text "Category" ]
            , select [ onInput UpdatePostCategory ]
                [ option [ value "coffee" ] [ text "Coffee" ]
                , option [ value "food" ] [ text "Food" ]
                , option [ value "drinks" ] [ text "Drinks" ]
                , option [ value "outdoor" ] [ text "Outdoor" ]
                , option [ value "fitness" ] [ text "Fitness" ]
                , option [ value "culture" ] [ text "Culture" ]
                , option [ value "work" ] [ text "Work" ]
                , option [ value "events" ] [ text "Events" ]
                , option [ value "other" ] [ text "Other" ]
                ]
            ]
        , div [ class "form-group" ]
            [ label []
                [ input
                    [ type_ "checkbox"
                    , checked model.postIsDate
                    , onCheck (\_ -> TogglePostIsDate)
                    ]
                    []
                , text " This is a romantic date (not platonic)"
                ]
            ]
        , button
            [ class "submit-btn"
            , onClick SubmitPost
            , disabled (String.trim model.postTitle == "" || String.trim model.postLocation == "")
            ]
            [ text "Create Post" ]
        ]


postsDisplaySection : Model -> Html Msg
postsDisplaySection model =
    div [ class "posts-section" ]
        [ h2 [] [ text "Activity Posts" ]
        , button
            [ class "publish-btn"
            , onClick ViewPosts
            ]
            [ text "View Posts" ]
        , case model.posts of
            Nothing ->
                text ""
            
            Just posts ->
                div [ class "result" ]
                    [ div [ class "posts-data" ]
                        [ text posts ]
                    ]
        ]