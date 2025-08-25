module Main exposing (main)

import Browser
import Browser.Navigation
import Html exposing (Html, button, div, form, h1, h2, input, option, p, select, span, text, textarea)
import Html.Attributes exposing (class, placeholder, type_, value)
import Html.Events exposing (onClick, onInput, onSubmit, preventDefaultOn)
import Http
import Json.Decode as Decode
import Json.Encode as Encode
import Url


type alias Post =
    { id : String
    , title : String
    , author : String
    , description : Maybe String
    , createdAt : String
    , endDate : String
    , groupSize : Int
    , tags : List String
    , location : Maybe String
    , proposedTime : String
    , isDateActivity : Bool
    , status : String
    , category : String
    }


type alias Model =
    { posts : List Post
    , showCreateForm : Bool
    , newPost : NewPost
    , error : Maybe String
    }


type alias NewPost =
    { title : String
    , description : String
    , location : String
    , groupSize : String
    , category : String
    }


type Msg
    = NoOp
    | PostsLoaded (Result Http.Error (List Post))
    | PostCreated (Result Http.Error ())
    | RefreshPosts
    | ShowCreateForm
    | HideCreateForm
    | UpdateTitle String
    | UpdateDescription String
    | UpdateLocation String
    | UpdateGroupSize String
    | UpdateCategory String
    | SubmitPost


init : flags -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init _ _ _ =
    ( { posts = [], showCreateForm = False, newPost = emptyNewPost, error = Nothing }, loadPosts )


emptyNewPost : NewPost
emptyNewPost =
    { title = ""
    , description = ""
    , location = ""
    , groupSize = "2"
    , category = "coffee"
    }


loadPosts : Cmd Msg
loadPosts =
    Http.get
        { url = "http://localhost:8080/api/Posts:getAll"
        , expect = Http.expectJson PostsLoaded postsDecoder
        }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )

        PostsLoaded result ->
            case result of
                Ok posts ->
                    ( { model | posts = posts, error = Nothing }, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "Error loading posts" err
                    in
                    ( { model | error = Just "Failed to load posts. Please check if the server is running." }, Cmd.none )
        
        PostCreated result ->
            case result of
                Ok _ ->
                    ( { model | showCreateForm = False, newPost = emptyNewPost }, loadPosts )
                Err err ->
                    let
                        _ =
                            Debug.log "Error creating post" err
                    in
                    ( { model | error = Just "Failed to create post. Please try again." }, Cmd.none )
        
        RefreshPosts ->
            ( { model | error = Nothing }, loadPosts )

        ShowCreateForm ->
            ( { model | showCreateForm = True }, Cmd.none )

        HideCreateForm ->
            ( { model | showCreateForm = False, newPost = emptyNewPost }, Cmd.none )

        UpdateTitle title ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | title = title } }, Cmd.none )

        UpdateDescription description ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | description = description } }, Cmd.none )

        UpdateLocation location ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | location = location } }, Cmd.none )

        UpdateGroupSize groupSize ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | groupSize = groupSize } }, Cmd.none )

        UpdateCategory category ->
            let
                newPost =
                    model.newPost
            in
            ( { model | newPost = { newPost | category = category } }, Cmd.none )

        SubmitPost ->
            if String.isEmpty (String.trim model.newPost.title) then
                ( model, Cmd.none )
            else
                ( model, createPost model.newPost )


view : Model -> Browser.Document Msg
view model =
    { title = "ThirdPlace"
    , body =
        [ div [ class "app" ]
            [ div [ class "header" ]
                [ h1 [ class "app-title" ] [ text "ThirdPlace" ]
                , p [ class "app-subtitle" ] [ text "Find people to join you for real-world activities" ]
                ]
            , div [ class "posts-container" ]
                [ div [ class "posts-header" ]
                    [ h2 [ class "posts-title" ] [ text "Recent Activities" ]
                    , button [ class "refresh-button", onClick RefreshPosts ] [ text "Refresh" ]
                    ]
                , case model.error of
                    Just errorMsg ->
                        div [ class "error-message" ] [ text errorMsg ]

                    Nothing ->
                        div [ class "posts-list" ] (List.map viewPost model.posts)
                ]
            , if model.showCreateForm then
                viewCreateForm model.newPost

              else
                button [ class "create-button", onClick ShowCreateForm ] [ text "+" ]
            ]
        ]
    }


viewPost : Post -> Html Msg
viewPost post =
    div [ class "post-card" ]
        [ div [ class "post-header" ]
            [ div [ class "post-title" ] [ text post.title ]
            , div [ class "post-meta" ]
                [ span [ class "post-author" ] [ text ("by " ++ post.author) ]
                , span [ class "post-category" ] [ text post.category ]
                ]
            ]
        , case post.description of
            Just desc ->
                p [ class "post-description" ] [ text desc ]

            Nothing ->
                text ""
        , div [ class "post-details" ]
            [ div [ class "post-detail" ]
                [ span [ class "detail-label" ] [ text "When:" ]
                , span [ class "detail-value" ] [ text post.proposedTime ]
                ]
            , case post.location of
                Just loc ->
                    div [ class "post-detail" ]
                        [ span [ class "detail-label" ] [ text "Where:" ]
                        , span [ class "detail-value" ] [ text loc ]
                        ]

                Nothing ->
                    text ""
            , div [ class "post-detail" ]
                [ span [ class "detail-label" ] [ text "Group size:" ]
                , span [ class "detail-value" ] [ text (String.fromInt post.groupSize ++ " people") ]
                ]
            ]
        , if List.isEmpty post.tags then
            text ""

          else
            div [ class "post-tags" ] (List.map viewTag post.tags)
        ]


viewTag : String -> Html Msg
viewTag tag =
    span [ class "tag" ] [ text tag ]

createPost : NewPost -> Cmd Msg
createPost newPost =
    Http.post
        { url = "http://localhost:8080/api/Posts:create"
        , body = Http.jsonBody (encodeNewPost newPost)
        , expect = Http.expectWhatever PostCreated
        }

encodeNewPost : NewPost -> Encode.Value
encodeNewPost newPost =
    Encode.object
        [ ( "title", Encode.string newPost.title )
        , ( "author", Encode.string "current_user" )
        , ( "description", Encode.string newPost.description )
        , ( "endDate", Encode.string "2025-01-28T23:59:00Z" )
        , ( "groupSize", Encode.int (String.toInt newPost.groupSize |> Maybe.withDefault 2) )
        , ( "tags", Encode.list Encode.string [] )
        , ( "location", Encode.string newPost.location )
        , ( "latitude", Encode.float 37.7749 )
        , ( "longitude", Encode.float -122.4194 )
        , ( "proposedTime", Encode.string "2025-01-28T15:00:00Z" )
        , ( "genderBalance", Encode.string "any" )
        , ( "category", Encode.string newPost.category )
        ]

postsDecoder : Decode.Decoder (List Post)
postsDecoder =
    Decode.field "posts" (Decode.list postDecoder)


postDecoder : Decode.Decoder Post
postDecoder =
    Decode.succeed Post
        |> andMap (Decode.field "id" Decode.string)
        |> andMap (Decode.field "title" Decode.string)
        |> andMap (Decode.field "author" Decode.string)
        |> andMap (Decode.maybe (Decode.field "description" Decode.string))
        |> andMap (Decode.field "createdAt" Decode.string)
        |> andMap (Decode.field "endDate" Decode.string)
        |> andMap (Decode.field "groupSize" Decode.int)
        |> andMap (Decode.field "tags" (Decode.list Decode.string))
        |> andMap (Decode.maybe (Decode.field "location" Decode.string))
        |> andMap (Decode.field "proposedTime" Decode.string)
        |> andMap (Decode.field "isDateActivity" Decode.bool)
        |> andMap (Decode.field "status" Decode.string)
        |> andMap (Decode.field "category" Decode.string)


andMap : Decode.Decoder a -> Decode.Decoder (a -> b) -> Decode.Decoder b
andMap =
    Decode.map2 (|>)


viewCreateForm : NewPost -> Html Msg
viewCreateForm newPost =
    div [ class "create-form-overlay" ]
        [ div [ class "create-form" ]
            [ div [ class "form-header" ]
                [ h2 [ class "form-title" ] [ text "Create Activity" ]
                , button [ class "cancel-button", onClick HideCreateForm ] [ text "×" ]
                ]
            , form [ onSubmit SubmitPost, preventDefaultOn "submit" (Decode.succeed ( SubmitPost, True )) ]
                [ div [ class "form-field" ]
                    [ input
                        [ type_ "text"
                        , placeholder "Activity title (e.g., Coffee at Blue Bottle)"
                        , value newPost.title
                        , onInput UpdateTitle
                        , class "form-input"
                        ]
                        []
                    ]
                , div [ class "form-field" ]
                    [ textarea
                        [ placeholder "Description (optional)"
                        , value newPost.description
                        , onInput UpdateDescription
                        , class "form-textarea"
                        ]
                        []
                    ]
                , div [ class "form-field" ]
                    [ input
                        [ type_ "text"
                        , placeholder "Location (e.g., Blue Bottle Coffee, 123 Main St)"
                        , value newPost.location
                        , onInput UpdateLocation
                        , class "form-input"
                        ]
                        []
                    ]
                , div [ class "form-row" ]
                    [ div [ class "form-field" ]
                        [ select
                            [ value newPost.groupSize
                            , onInput UpdateGroupSize
                            , class "form-select"
                            ]
                            [ option [ value "2" ] [ text "2 people" ]
                            , option [ value "3" ] [ text "3 people" ]
                            , option [ value "4" ] [ text "4 people" ]
                            , option [ value "5" ] [ text "5 people" ]
                            ]
                        ]
                    , div [ class "form-field" ]
                        [ select
                            [ value newPost.category
                            , onInput UpdateCategory
                            , class "form-select"
                            ]
                            [ option [ value "coffee" ] [ text "Coffee" ]
                            , option [ value "food" ] [ text "Food" ]
                            , option [ value "drinks" ] [ text "Drinks" ]
                            , option [ value "outdoor" ] [ text "Outdoor" ]
                            , option [ value "fitness" ] [ text "Fitness" ]
                            , option [ value "culture" ] [ text "Culture" ]
                            ]
                        ]
                    ]
                , div [ class "form-actions" ]
                    [ button [ type_ "button", class "cancel-btn", onClick HideCreateForm ] [ text "Cancel" ]
                    , button [ type_ "submit", class "submit-btn" ] [ text "Create Activity" ]
                    ]
                ]
            ]
        ]


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
